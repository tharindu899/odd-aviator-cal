package com.aviator.hoc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {

    @Query("SELECT * FROM signals ORDER BY targetMs DESC")
    fun observeAll(): Flow<List<SignalEntity>>

    @Insert
    suspend fun insert(signal: SignalEntity): Long

    @Query("SELECT COUNT(*) FROM signals WHERE status = 'pending' AND targetMs = :targetMs")
    suspend fun countPendingAt(targetMs: Long): Int

    @Query("UPDATE signals SET status = :status, autoMarked = :auto, updatedAt = :now WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, auto: Boolean, now: Long)

    /** Pending signals whose WIN/LOSS window is over: targetMs + 45s + 5s <= now */
    @Query(
        "UPDATE signals SET status = 'missed', autoMarked = 1, updatedAt = :now " +
            "WHERE status = 'pending' AND targetMs + :graceMs <= :now"
    )
    suspend fun markExpiredAsMissed(now: Long, graceMs: Long): Int

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM signals")
    suspend fun clear()

    @Insert
    suspend fun insertAll(items: List<SignalEntity>)
}
