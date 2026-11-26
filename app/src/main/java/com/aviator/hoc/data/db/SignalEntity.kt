package com.aviator.hoc.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object SignalStatus {
    const val PENDING = "pending"
    const val WIN = "win"
    const val LOSS = "loss"
    const val MISSED = "missed"
}

@Entity(
    tableName = "signals",
    indices = [Index("status"), Index("targetMs")],
)
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The odd the user typed, e.g. 2.02 */
    val odd: Double,
    /** The original time the odd happened at (seconds since midnight) */
    val originalSeconds: Int,
    /** Absolute moment the signal lands (epoch ms, local zone resolved at creation) */
    val targetMs: Long,
    val status: String = SignalStatus.PENDING,
    /** true when the app (not the user) marked it missed */
    val autoMarked: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
