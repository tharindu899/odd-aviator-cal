package com.aviator.hoc.domain

import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmPlanTest {
    private fun sig(id: Long, target: Long, status: String = SignalStatus.PENDING, created: Long = id) =
        SignalEntity(id = id, odd = 2.0, originalSeconds = 0, targetMs = target, status = status, createdAt = created, updatedAt = created)

    @Test
    fun triggersOneMinuteBeforeSignal() {
        val plan = planAlarms(listOf(sig(1, 500_000)), nowMs = 0)
        assertEquals(1, plan.size)
        assertEquals(440_000L, plan[0].triggerMs)
    }

    @Test
    fun skipsPastNonPendingAndDuplicates() {
        val list = listOf(
            sig(1, 30_000),                              // alert moment already passed
            sig(2, 500_000, SignalStatus.WIN),           // not pending
            sig(3, 900_000, created = 5),
            sig(4, 900_000, created = 2),                // same time, created earlier -> kept
        )
        val plan = planAlarms(list, nowMs = 0)
        assertEquals(listOf(4L), plan.map { it.id })
    }

    @Test
    fun sortedAndCapped() {
        val list = (1L..10L).map { sig(it, 1_000_000 - it * 10_000) }
        val plan = planAlarms(list, nowMs = 0, max = 3)
        assertEquals(listOf(10L, 9L, 8L), plan.map { it.id })
    }
}
