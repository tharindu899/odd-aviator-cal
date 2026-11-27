package com.aviator.hoc.domain

import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.WindowTimes
import com.aviator.hoc.core.countdownSeconds
import com.aviator.hoc.core.hmsOfEpoch
import com.aviator.hoc.core.windowTimes
import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

enum class StatusFilter(val label: String, val status: String?) {
    ALL("All", null),
    ACTIVE("Active", SignalStatus.PENDING),
    WON("Won", SignalStatus.WIN),
    LOST("Lost", SignalStatus.LOSS),
    MISSED("Missed", SignalStatus.MISSED),
}

enum class SortMode(val label: String) {
    ACTIVE_FIRST("Active first"),
    NEWEST("Newest"),
    OLDEST("Oldest"),
}

/** One list row: the stored signal + what is derived from "now". */
data class ResultRow(
    val signal: SignalEntity,
    val resultTime: Hms,
    val times: WindowTimes,
    /** only for pending signals */
    val countdown: Long?,
    val active: Boolean,
    val created: String,
)

// Words that can be typed in the search box for a status (stored value AND what the chips show)
private val STATUS_WORDS = mapOf(
    SignalStatus.WIN to "win won",
    SignalStatus.LOSS to "loss lost lose",
    SignalStatus.MISSED to "missed miss",
    SignalStatus.PENDING to "pending active",
)

private val CREATED_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy, HH:mm:ss", Locale.US)

fun formatCreated(ms: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    CREATED_FORMAT.format(Instant.ofEpochMilli(ms).atZone(zone))

/** 150.0 -> "150", 2.02 -> "2.02" (same text JavaScript prints). */
fun oddText(odd: Double): String =
    if (odd == Math.floor(odd) && !odd.isInfinite()) odd.toLong().toString() else odd.toString()

/** "Active now": pending and inside the 45 s before / 45 s after window. */
fun isActiveNow(status: String, countdown: Long?): Boolean =
    status == SignalStatus.PENDING && countdown != null && countdown > -45 && countdown <= 45

fun filterCounts(list: List<SignalEntity>): Map<StatusFilter, Int> =
    StatusFilter.entries.associateWith { f -> if (f.status == null) list.size else list.count { it.status == f.status } }

fun filterAndSort(
    list: List<SignalEntity>,
    filter: StatusFilter,
    query: String,
    sort: SortMode,
    nowMs: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): List<SignalEntity> {
    var r = list
    filter.status?.let { st -> r = r.filter { it.status == st } }

    val q = query.trim().lowercase()
    if (q.isNotEmpty()) {
        r = r.filter {
            oddText(it.odd).contains(q) ||
                it.status.lowercase().contains(q) ||
                (STATUS_WORDS[it.status] ?: "").contains(q) ||
                hmsOfEpoch(it.targetMs, zone).toString().contains(q) ||
                formatCreated(it.createdAt, zone).lowercase().contains(q)
        }
    }

    return when (sort) {
        SortMode.NEWEST -> r.sortedByDescending { it.createdAt }
        SortMode.OLDEST -> r.sortedBy { it.createdAt }
        SortMode.ACTIVE_FIRST -> r.sortedWith { a, b -> activeFirst(a, b, nowMs) }
    }
}

private fun activeFirst(a: SignalEntity, b: SignalEntity, nowMs: Long): Int {
    val ca = if (a.status == SignalStatus.PENDING) countdownSeconds(a.targetMs, nowMs) else null
    val cb = if (b.status == SignalStatus.PENDING) countdownSeconds(b.targetMs, nowMs) else null
    val activeA = isActiveNow(a.status, ca)
    val activeB = isActiveNow(b.status, cb)

    if (activeA && !activeB) return -1
    if (!activeA && activeB) return 1
    if (activeA && activeB) return abs(ca!!).compareTo(abs(cb!!))

    val doneA = a.status != SignalStatus.PENDING
    val doneB = b.status != SignalStatus.PENDING
    if (doneA && doneB) return b.targetMs.compareTo(a.targetMs)   // most recent first
    if (doneA && !doneB) return -1                                // settled before waiting
    if (!doneA && doneB) return 1
    return a.targetMs.compareTo(b.targetMs)                       // waiting: soonest first
}

fun toRow(e: SignalEntity, nowMs: Long, zone: ZoneId = ZoneId.systemDefault()): ResultRow {
    val cd = if (e.status == SignalStatus.PENDING) countdownSeconds(e.targetMs, nowMs) else null
    val rt = hmsOfEpoch(e.targetMs, zone)
    return ResultRow(
        signal = e,
        resultTime = rt,
        times = windowTimes(rt),
        countdown = cd,
        active = isActiveNow(e.status, cd),
        created = formatCreated(e.createdAt, zone),
    )
}

/** Pretty JSON (same idea as the web app's "Export JSON"). */
fun exportJson(list: List<SignalEntity>, zone: ZoneId = ZoneId.systemDefault()): String {
    if (list.isEmpty()) return "[]"
    return list.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { e ->
        val original = Hms(e.originalSeconds / 3600, (e.originalSeconds % 3600) / 60, e.originalSeconds % 60)
        val date = Instant.ofEpochMilli(e.targetMs).atZone(zone).toLocalDate()
        """  {
    "id": ${e.id},
    "odd": ${e.odd},
    "originalTime": "$original",
    "resultTime": "${hmsOfEpoch(e.targetMs, zone)}",
    "resultDate": "$date",
    "status": "${e.status}",
    "autoMarked": ${e.autoMarked},
    "createdAt": "${Instant.ofEpochMilli(e.createdAt)}",
    "updatedAt": "${Instant.ofEpochMilli(e.updatedAt)}"
  }"""
    }
}
