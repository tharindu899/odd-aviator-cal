package com.aviator.hoc.domain

import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.WindowTimes
import com.aviator.hoc.core.computeSignal
import com.aviator.hoc.core.parseInput
import com.aviator.hoc.core.secondsSinceClose
import com.aviator.hoc.core.windowTimes
import java.time.Instant
import java.time.ZoneId

/** What the Generate screen shows while typing - same maths the repository uses when saving. */
data class GeneratePreview(
    val odd: Double,
    val resultTime: Hms,
    /** the signal lands after midnight ("+1d") */
    val nextDay: Boolean,
    val window: WindowTimes,
    /** the whole bet window is already over, the signal would instantly become "missed" */
    val alreadyPassed: Boolean,
)

fun previewFor(
    text: String,
    nowMs: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): GeneratePreview? {
    val parsed = parseInput(text) ?: return null
    val today = Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
    val c = computeSignal(parsed, today, zone)
    return GeneratePreview(
        odd = parsed.odd,
        resultTime = c.resultTime,
        nextDay = c.daysOffset > 0,
        window = windowTimes(c.resultTime),
        alreadyPassed = secondsSinceClose(c.targetMs, nowMs) >= 0,
    )
}

private val GLUED = Regex("([0-9.]+)x(\\d)", RegexOption.IGNORE_CASE)

/** "2.02x21:31:22" (copied without a space) -> "2.02x 21:31:22" */
fun normalizePasted(raw: String): String = raw.trim().replace(GLUED, "\$1x \$2")
