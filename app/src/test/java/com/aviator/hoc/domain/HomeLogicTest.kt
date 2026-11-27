package com.aviator.hoc.domain

import com.aviator.hoc.core.WindowStatus
import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HomeLogicTest {
    private val zone = ZoneId.of("UTC")
    private val now = 1_800_000_000_000L

    private fun sig(
        id: Long,
        targetOffsetSec: Long,
        status: String = SignalStatus.PENDING,
        created: Long = id,
    ) = SignalEntity(
        id = id, odd = 2.02, originalSeconds = 0,
        targetMs = now + targetOffsetSec * 1000, status = status,
        createdAt = created, updatedAt = created,
    )

    @Test fun upcoming_hidesClosed_nonPending_andFarFuture_sortedNearestFirst() {
        val list = listOf(
            sig(1, 600),
            sig(2, 60),
            sig(3, -100),                         // window already closed (closes at +45 s)
            sig(4, 30, SignalStatus.WIN),
            sig(5, 8 * 24 * 3600L),               // > 7 days away
            sig(6, -40),                          // inside grace period, still shown
        )
        val up = upcomingSignals(list, now, zone)
        assertEquals(listOf(6L, 2L, 1L), up.map { it.id })
    }

    @Test fun upcoming_dedupesSameTime_keepingEarliestCreated() {
        val list = listOf(sig(10, 120, created = 500), sig(11, 120, created = 100))
        assertEquals(listOf(11L), upcomingSignals(list, now, zone).map { it.id })
    }

    @Test fun current_prefersBetNowOverCloserWaiting() {
        val list = listOf(sig(1, 200), sig(2, 20))   // 2 is ACTIVE (<= 45 s)
        val up = upcomingSignals(list, now, zone)
        assertEquals(2L, currentSignal(up)?.id)
        assertEquals(WindowStatus.ACTIVE, currentSignal(up)?.status)
    }

    @Test fun current_isClosestWaiting_whenNothingActive() {
        val up = upcomingSignals(listOf(sig(1, 300), sig(2, 120)), now, zone)
        assertEquals(2L, currentSignal(up)?.id)
    }

    @Test fun nearby_countsSignalsWithin2Minutes() {
        val up = upcomingSignals(listOf(sig(1, 100), sig(2, 180), sig(3, 400)), now, zone)
        val cur = currentSignal(up)!!
        assertEquals(1, nearbyCount(cur, up))
    }

    @Test fun markInfo_appearsFrom25sBeforeClose_untilClosePlus10() {
        // closes at target + 45 s
        val early = markInfo(listOf(sig(1, 20)), now, zone)      // close in 65 s -> too early
        assertNull(early)
        val open = markInfo(listOf(sig(1, -20)), now, zone)      // close in 25 s -> shown
        assertNotNull(open)
        assertEquals(35, open!!.secondsLeft)   // 25 s before close + 10 s after
        val after = markInfo(listOf(sig(1, -48)), now, zone)     // closed 3 s ago -> 7 s left
        assertEquals(7, after!!.secondsLeft)
        assertNull(markInfo(listOf(sig(1, -56)), now, zone))     // closed 11 s ago -> over
    }

    @Test fun markInfo_usesCustomDelay() {
        val m = markInfo(listOf(sig(1, -48)), now, zone, markAfterSeconds = 20)   // closed 3 s ago -> 17 s left
        assertEquals(17, m!!.secondsLeft)
        assertNull(markInfo(listOf(sig(1, -48)), now, zone, markAfterSeconds = 3)) // 3 s delay already over
    }

    @Test fun windowLeft_isWholeNinetySecondWindow() {
        val opening = upcomingSignals(listOf(sig(1, 45)), now, zone).first()
        assertEquals(90L, opening.windowLeft)                       // 45 + 45 = 90 s = 1:30
        val mid = upcomingSignals(listOf(sig(2, 0)), now, zone).first()
        assertEquals(45L, mid.windowLeft)
        val grace = upcomingSignals(listOf(sig(3, -30)), now, zone).first()
        assertEquals(15L, grace.windowLeft)
    }

    @Test fun markInfo_ignoresNonPending() {
        assertNull(markInfo(listOf(sig(1, -20, SignalStatus.WIN)), now, zone))
    }

    @Test fun readout_countsToOpen_thenWholeWindow() {
        val waiting = upcomingSignals(listOf(sig(1, 100)), now, zone).first()
        assertEquals(55L, heroReadoutSeconds(waiting, now))         // opens in 100 - 45
        val active = upcomingSignals(listOf(sig(2, 0)), now, zone).first()
        assertEquals(45L, heroReadoutSeconds(active, now))          // window ends in 45 s
        val opening = upcomingSignals(listOf(sig(3, 45)), now, zone).first()
        assertEquals(90L, heroReadoutSeconds(opening, now))         // just opened: full 90 s window
    }

    @Test fun progress_spansTheNinetySecondWindow() {
        val opening = upcomingSignals(listOf(sig(1, 45)), now, zone).first()
        assertEquals(0f, windowProgress(opening, now), 0.001f)
        val mid = upcomingSignals(listOf(sig(2, 0)), now, zone).first()
        assertEquals(0.5f, windowProgress(mid, now), 0.001f)
        val waiting = upcomingSignals(listOf(sig(3, 500)), now, zone).first()
        assertEquals(0f, windowProgress(waiting, now), 0.001f)
    }
}
