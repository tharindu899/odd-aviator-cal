package com.aviator.hoc.domain

import com.aviator.hoc.core.Hms
import com.aviator.hoc.core.WindowStatus
import com.aviator.hoc.core.windowTimes
import com.aviator.hoc.data.SignalRepository
import org.junit.Test
import org.junit.Assert.assertEquals

class AlertTrackerTest {
    private fun live(id: Long, status: WindowStatus): LiveSignal {
        val rt = Hms(12, 0, 0)
        return LiveSignal(id, 2.0, 0L, 0L, rt, 0L, status, windowTimes(rt))
    }

    private fun state(current: LiveSignal?, mark: MarkInfo? = null) = HomeUiState(
        current = current, nearby = 0, upcoming = listOfNotNull(current), mark = mark,
        stats = SignalRepository.statsOf(emptyList()), readoutSeconds = 0, progress = 0f,
    )

    @Test
    fun firstStateNeverFires() {
        val t = AlertTracker()
        assertEquals(emptyList(), t.onState(state(live(1, WindowStatus.ACTIVE))))
    }

    @Test
    fun windowOpenFiresOncePerSignal() {
        val t = AlertTracker()
        t.onState(state(live(1, WindowStatus.WAITING)))
        assertEquals(listOf(AlertEvent.WINDOW_OPEN), t.onState(state(live(1, WindowStatus.ACTIVE))))
        assertEquals(emptyList(), t.onState(state(live(1, WindowStatus.ACTIVE))))
        assertEquals(emptyList(), t.onState(state(live(1, WindowStatus.GRACE))))
        t.onState(state(live(2, WindowStatus.WAITING)))
        assertEquals(listOf(AlertEvent.WINDOW_OPEN), t.onState(state(live(2, WindowStatus.ACTIVE))))
    }

    @Test
    fun markFiresOncePerSignal() {
        val t = AlertTracker()
        val s = live(1, WindowStatus.GRACE)
        t.onState(state(s))
        assertEquals(listOf(AlertEvent.MARK_OPEN), t.onState(state(s, MarkInfo(s, 20))))
        assertEquals(emptyList(), t.onState(state(s, MarkInfo(s, 19))))
        t.onState(state(null))
        assertEquals(emptyList(), t.onState(state(s, MarkInfo(s, 3))))
    }
}
