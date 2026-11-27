package com.aviator.hoc.data

import com.aviator.hoc.core.MARK_SECONDS
import com.aviator.hoc.core.WINDOW_SECONDS
import com.aviator.hoc.core.computeSignal
import com.aviator.hoc.core.parseInput
import com.aviator.hoc.data.db.SignalDao
import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

data class Stats(
    val total: Int,
    val today: Int,
    val pending: Int,
    val wins: Int,
    val losses: Int,
    val missed: Int,
    val completed: Int,
    val winRate: Int,
)

sealed interface GenerateResult {
    data class Success(val id: Long) : GenerateResult
    data class Error(val message: String) : GenerateResult
}

class SignalRepository(private val dao: SignalDao) {

    val signals: Flow<List<SignalEntity>> = dao.observeAll()

    /** Same rules as the web app's generateSignal: parse -> compute -> reject duplicates -> save. */
    suspend fun generate(
        input: String,
        nowMs: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): GenerateResult {
        val parsed = parseInput(input)
            ?: return GenerateResult.Error("Invalid format! Use: 2.02x 21:31:22 or 150x 23:34:00")

        val today = java.time.Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
        val computed = computeSignal(parsed, today, zone)

        if (dao.countPendingAt(computed.targetMs) > 0) {
            return GenerateResult.Error("This signal already exists! Each time can only be generated once.")
        }

        val id = dao.insert(
            SignalEntity(
                odd = parsed.odd,
                originalSeconds = parsed.hour * 3600 + parsed.minute * 60 + parsed.second,
                targetMs = computed.targetMs,
                createdAt = nowMs,
                updatedAt = nowMs,
            )
        )
        return GenerateResult.Success(id)
    }

    suspend fun mark(id: Long, status: String, nowMs: Long = System.currentTimeMillis()) =
        dao.setStatus(id, status, auto = false, now = nowMs)

    /** Pending signals whose WIN/LOSS window is over become MISSED. Returns how many changed. */
    suspend fun markExpiredAsMissed(
        nowMs: Long = System.currentTimeMillis(),
        markAfterSeconds: Int = MARK_SECONDS,
    ): Int = dao.markExpiredAsMissed(nowMs, (WINDOW_SECONDS + markAfterSeconds) * 1000L)

    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun clearAll() = dao.clear()

    companion object {
        fun statsOf(
            list: List<SignalEntity>,
            nowMs: Long = System.currentTimeMillis(),
            zone: ZoneId = ZoneId.systemDefault(),
        ): Stats {
            val today: LocalDate = java.time.Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
            val wins = list.count { it.status == SignalStatus.WIN }
            val losses = list.count { it.status == SignalStatus.LOSS }
            val completed = wins + losses
            return Stats(
                total = list.size,
                today = list.count {
                    java.time.Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() == today
                },
                pending = list.count { it.status == SignalStatus.PENDING },
                wins = wins,
                losses = losses,
                missed = list.count { it.status == SignalStatus.MISSED },
                completed = completed,
                winRate = if (completed > 0) Math.round(wins * 100.0 / completed).toInt() else 0,
            )
        }
    }
}
