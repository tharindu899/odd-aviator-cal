package com.aviator.hoc.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Pure time logic ported 1:1 from the web app (utils/timeCalculations.js).
 * Everything here is free of Android classes so it can be unit-tested on the JVM.
 */

/** Bet window = 45 s before the signal time to 45 s after it. */
const val WINDOW_SECONDS = 45

/** Default: after the window closes, WIN / LOSS stay available for this many seconds (then auto-miss). */
const val MARK_SECONDS = 10

/** Allowed range for the user-chosen auto-miss delay (Settings). */
const val MARK_SECONDS_MIN = 1
const val MARK_SECONDS_MAX = 20

/** WIN / LOSS appear this many seconds BEFORE the window closes. */
const val MARK_BEFORE_SECONDS = 25

data class Hms(val hours: Int, val minutes: Int, val seconds: Int) {
    val totalSeconds: Int get() = hours * 3600 + minutes * 60 + seconds
    override fun toString(): String = String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
}

data class ParsedInput(val odd: Double, val hour: Int, val minute: Int, val second: Int)

data class WindowTimes(val open: Hms, val signal: Hms, val close: Hms)

enum class WindowStatus { WAITING, ACTIVE, GRACE, CLOSED }

fun hmsOfDaySeconds(daySeconds: Int): Hms {
    val s = ((daySeconds % 86400) + 86400) % 86400
    return Hms(s / 3600, (s % 3600) / 60, s % 60)
}

/** "2.02x" -> 00:02:02 (the odd is read as minutes.seconds with the odd's own quirk kept). */
fun oddToTime(odd: Double): Hms {
    var hours = 0
    var minutes = ceil(odd).toInt()
    val decimalPart = odd - floor(odd)
    var seconds = Math.round(decimalPart * 100 - 60).toInt()

    if (seconds < 0) {
        minutes -= 1
        seconds += 60
    }
    if (minutes >= 60) {
        hours = minutes / 60
        minutes %= 60
    }
    hours %= 24
    return Hms(hours, minutes, seconds)
}

private val INPUT_PATTERN = Regex("""([0-9.]+)x?\s*(\d{1,2}):(\d{1,2}):(\d{1,2})""", RegexOption.IGNORE_CASE)

/** Parses text like "2.02x 21:31:22", "150x 23:34:00" or "1,437.20x 06:31:56". */
fun parseInput(input: String?): ParsedInput? {
    if (input.isNullOrBlank()) return null
    val cleaned = input.trim().replace(Regex("""\s+"""), " ").replace(",", "")
    val m = INPUT_PATTERN.find(cleaned) ?: return null

    val odd = m.groupValues[1].toDoubleOrNull() ?: return null
    val hour = m.groupValues[2].toInt()
    val minute = m.groupValues[3].toInt()
    val second = m.groupValues[4].toInt()

    if (odd <= 0 || hour !in 0..23 || minute !in 0..59 || second !in 0..59) return null
    return ParsedInput(odd, hour, minute, second)
}

/** Result of turning a parsed input into an absolute signal moment. */
data class ComputedSignal(val targetMs: Long, val resultTime: Hms, val daysOffset: Int)

/**
 * original time + oddToTime(odd). If it runs past midnight the signal lands on a later day,
 * exactly like the web app (date = today + daysOffset, time = total % 86400).
 */
fun computeSignal(
    parsed: ParsedInput,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): ComputedSignal {
    val add = oddToTime(parsed.odd)
    val total = parsed.hour * 3600 + parsed.minute * 60 + parsed.second + add.totalSeconds
    val days = total / 86400
    val resultTime = hmsOfDaySeconds(total)
    val target = today.plusDays(days.toLong())
        .atTime(LocalTime.of(resultTime.hours, resultTime.minutes, resultTime.seconds))
        .atZone(zone)
        .toInstant()
        .toEpochMilli()
    return ComputedSignal(target, resultTime, days)
}

/** Whole seconds until the signal (negative once passed). */
fun countdownSeconds(targetMs: Long, nowMs: Long): Long = Math.floorDiv(targetMs - nowMs, 1000L)

/** Exact seconds since the bet window CLOSED. Negative while it is still open. */
fun secondsSinceClose(targetMs: Long, nowMs: Long): Double =
    (nowMs - targetMs) / 1000.0 - WINDOW_SECONDS

fun isInMarkWindow(targetMs: Long, nowMs: Long, markAfterSeconds: Int = MARK_SECONDS): Boolean {
    val s = secondsSinceClose(targetMs, nowMs)
    return s >= -MARK_BEFORE_SECONDS && s < markAfterSeconds
}

/** Mark window finished: a still-pending signal now counts as MISSED. */
fun isMarkWindowOver(targetMs: Long, nowMs: Long, markAfterSeconds: Int = MARK_SECONDS): Boolean =
    secondsSinceClose(targetMs, nowMs) >= markAfterSeconds

fun windowStatus(countdown: Long): WindowStatus = when {
    countdown > WINDOW_SECONDS -> WindowStatus.WAITING
    countdown >= 0 -> WindowStatus.ACTIVE
    countdown >= -WINDOW_SECONDS -> WindowStatus.GRACE
    else -> WindowStatus.CLOSED
}

val WindowStatus.canBet: Boolean get() = this == WindowStatus.ACTIVE || this == WindowStatus.GRACE

/** Seconds left until the window closes (only meaningful while ACTIVE/GRACE). */
fun windowRemainingSeconds(countdown: Long): Long = countdown + WINDOW_SECONDS

/** Seconds until the window opens (only meaningful while WAITING). */
fun windowOpensInSeconds(countdown: Long): Long = countdown - WINDOW_SECONDS

fun windowTimes(resultTime: Hms): WindowTimes = WindowTimes(
    open = hmsOfDaySeconds(resultTime.totalSeconds - WINDOW_SECONDS),
    signal = resultTime,
    close = hmsOfDaySeconds(resultTime.totalSeconds + WINDOW_SECONDS),
)

fun hmsOfEpoch(ms: Long, zone: ZoneId = ZoneId.systemDefault()): Hms {
    val t = Instant.ofEpochMilli(ms).atZone(zone).toLocalTime()
    return Hms(t.hour, t.minute, t.second)
}

/** 125 -> "02:05", 3725 -> "1:02:05" */
fun formatClock(totalSeconds: Long): String {
    val s = maxOf(0L, totalSeconds)
    val h = s / 3600
    val m = (s % 3600) / 60
    val r = s % 60
    return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, r) else String.format(Locale.ROOT, "%02d:%02d", m, r)
}

/** Same wording as the web app's formatCountdown. */
fun formatCountdown(seconds: Long): String {
    if (seconds < -30) return "Passed"
    if (seconds < 0) return "${-seconds}s ago"
    if (seconds == 0L) return "NOW!"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> "${h}h ${m}m ${s}s"
        m > 0 -> "${m}m ${s}s"
        else -> "${s}s"
    }
}
