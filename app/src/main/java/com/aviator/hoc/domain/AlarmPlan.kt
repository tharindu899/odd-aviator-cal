package com.aviator.hoc.domain

import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus

/** Notify this many seconds before the signal time. */
const val NOTIFY_LEAD_SECONDS = 60

data class PlannedAlarm(val id: Long, val triggerMs: Long, val targetMs: Long)

/**
 * Which notifications should be scheduled: pending signals whose alert moment is still in the
 * future. Same-time duplicates collapse to one (the earliest created), and only the nearest
 * [max] are kept (AlarmManager has a per-app limit).
 */
fun planAlarms(list: List<SignalEntity>, nowMs: Long, max: Int = 40): List<PlannedAlarm> =
    list.asSequence()
        .filter { it.status == SignalStatus.PENDING }
        .groupBy { it.targetMs }
        .values
        .map { g -> g.minBy { it.createdAt } }
        .map { PlannedAlarm(it.id, it.targetMs - NOTIFY_LEAD_SECONDS * 1000L, it.targetMs) }
        .filter { it.triggerMs > nowMs }
        .sortedBy { it.triggerMs }
        .take(max)
