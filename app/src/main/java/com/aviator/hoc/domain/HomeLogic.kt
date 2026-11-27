package com.aviator.hoc.domain

import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.MARK_BEFORE_SECONDS
import com.aviator.hoc.core.MARK_SECONDS
import com.aviator.hoc.core.WINDOW_SECONDS
import com.aviator.hoc.core.WindowStatus
import com.aviator.hoc.core.WindowTimes
import com.aviator.hoc.core.canBet
import com.aviator.hoc.core.countdownSeconds
import com.aviator.hoc.core.hmsOfEpoch
import com.aviator.hoc.core.isInMarkWindow
import com.aviator.hoc.core.secondsSinceClose
import com.aviator.hoc.core.windowOpensInSeconds
import com.aviator.hoc.core.windowStatus
import com.aviator.hoc.core.windowTimes
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.Stats
import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

/** A stored signal + everything derived from "now" (countdown, window state, clock times). */
data class LiveSignal(
    val id: Long,
    val odd: Double,
    val targetMs: Long,
    val createdAt: Long,
    val resultTime: Hms,
    val countdown: Long,
    val status: WindowStatus,
    val times: WindowTimes,
    /** whole bet window left, open -> close (90 s .. 0 s), same number the hero card shows */
    val windowLeft: Long = 0L,
)

data class MarkInfo(val signal: LiveSignal, val secondsLeft: Int)

data class HomeUiState(
    val current: LiveSignal?,
    /** other signals within 2 minutes of the current one */
    val nearby: Int,
    val upcoming: List<LiveSignal>,
    val mark: MarkInfo?,
    val stats: Stats,
    /** the big number: seconds until the window opens, or until it closes */
    val readoutSeconds: Long,
    /** 0..1 over the 90 s window (45 s before -> 45 s after the signal) */
    val progress: Float,
) {
    companion object {
        val Empty = HomeUiState(
            current = null, nearby = 0, upcoming = emptyList(), mark = null,
            stats = SignalRepository.statsOf(emptyList()), readoutSeconds = 0, progress = 0f,
        )
    }
}

private const val SEVEN_DAYS_SECONDS = 7L * 24 * 60 * 60

fun SignalEntity.toLive(nowMs: Long, zone: ZoneId = ZoneId.systemDefault()): LiveSignal {
    val cd = countdownSeconds(targetMs, nowMs)
    val rt = hmsOfEpoch(targetMs, zone)
    val left = ceil(max(0.0, -secondsSinceClose(targetMs, nowMs))).toLong()
    return LiveSignal(id, odd, targetMs, createdAt, rt, cd, windowStatus(cd), windowTimes(rt), left)
}

/**
 * Pending signals whose bet window has not closed yet, nearest first.
 * Same-time duplicates collapse to the earliest-created one. Anything > 7 days away is hidden.
 */
fun upcomingSignals(list: List<SignalEntity>, nowMs: Long, zone: ZoneId = ZoneId.systemDefault()): List<LiveSignal> =
    list.asSequence()
        .filter { it.status == SignalStatus.PENDING }
        .filter { secondsSinceClose(it.targetMs, nowMs) < 0 }
        .groupBy { it.targetMs }
        .values
        .map { group -> group.minBy { it.createdAt } }
        .map { it.toLive(nowMs, zone) }
        .filter { it.countdown < SEVEN_DAYS_SECONDS }
        .sortedBy { it.countdown }

/** Active / grace signals win; otherwise the closest waiting one. */
fun currentSignal(upcoming: List<LiveSignal>): LiveSignal? =
    upcoming.firstOrNull { it.status.canBet } ?: upcoming.firstOrNull()

fun nearbyCount(current: LiveSignal, upcoming: List<LiveSignal>): Int =
    upcoming.count { it.id != current.id && abs(it.targetMs - current.targetMs) <= 120_000L }

/** The pending signal WIN / LOSS should be offered for (closed-first if several overlap). */
fun markInfo(
    list: List<SignalEntity>,
    nowMs: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    markAfterSeconds: Int = MARK_SECONDS,
): MarkInfo? {
    val s = list
        .filter { it.status == SignalStatus.PENDING && isInMarkWindow(it.targetMs, nowMs, markAfterSeconds) }
        .maxByOrNull { secondsSinceClose(it.targetMs, nowMs) }
        ?: return null
    val total = markAfterSeconds + MARK_BEFORE_SECONDS
    val left = ceil(markAfterSeconds - secondsSinceClose(s.targetMs, nowMs)).toInt().coerceIn(0, total)
    return MarkInfo(s.toLive(nowMs, zone), left)
}

fun heroReadoutSeconds(s: LiveSignal, nowMs: Long): Long =
    if (s.status == WindowStatus.WAITING) {
        windowOpensInSeconds(s.countdown)
    } else {
        // bet window running: count the WHOLE window down to 00:00
        ceil(max(0.0, -secondsSinceClose(s.targetMs, nowMs))).toLong()
    }

fun windowProgress(s: LiveSignal, nowMs: Long): Float {
    val full = 2.0 * WINDOW_SECONDS
    return ((secondsSinceClose(s.targetMs, nowMs) + full) / full).coerceIn(0.0, 1.0).toFloat()
}

fun buildHomeState(
    list: List<SignalEntity>,
    nowMs: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    markAfterSeconds: Int = MARK_SECONDS,
): HomeUiState {
    val upcoming = upcomingSignals(list, nowMs, zone)
    val current = currentSignal(upcoming)
    return HomeUiState(
        current = current,
        nearby = current?.let { nearbyCount(it, upcoming) } ?: 0,
        upcoming = upcoming,
        mark = markInfo(list, nowMs, zone, markAfterSeconds),
        stats = SignalRepository.statsOf(list, nowMs, zone),
        readoutSeconds = current?.let { heroReadoutSeconds(it, nowMs) } ?: 0L,
        progress = current?.let { windowProgress(it, nowMs) } ?: 0f,
    )
}
