package com.aviator.hoc.domain

import com.aviator.hoc.data.db.SignalEntity
import com.aviator.hoc.data.db.SignalStatus
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultsLogicTest {
    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2026-11-26T12:00:00Z").toEpochMilli()

    private fun e(
        id: Long,
        offsetSec: Long,
        status: String = SignalStatus.PENDING,
        odd: Double = 2.02,
        created: Long = id * 1000,
    ) = SignalEntity(
        id = id, odd = odd, originalSeconds = 12 * 3600, targetMs = now + offsetSec * 1000,
        status = status, createdAt = created, updatedAt = created,
    )

    private fun ids(l: List<SignalEntity>) = l.map { it.id }

    @Test fun filter_byStatus() {
        val l = listOf(e(1, 100), e(2, -500, SignalStatus.WIN), e(3, -600, SignalStatus.LOSS), e(4, -700, SignalStatus.MISSED))
        assertEquals(listOf(2L), ids(filterAndSort(l, StatusFilter.WON, "", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(1L), ids(filterAndSort(l, StatusFilter.ACTIVE, "", SortMode.NEWEST, now, zone)))
        assertEquals(4, filterAndSort(l, StatusFilter.ALL, "", SortMode.NEWEST, now, zone).size)
    }

    @Test fun counts_perFilter() {
        val l = listOf(e(1, 100), e(2, -500, SignalStatus.WIN), e(3, -600, SignalStatus.WIN))
        val c = filterCounts(l)
        assertEquals(3, c[StatusFilter.ALL])
        assertEquals(1, c[StatusFilter.ACTIVE])
        assertEquals(2, c[StatusFilter.WON])
        assertEquals(0, c[StatusFilter.LOST])
    }

    @Test fun search_statusWords_odd_time() {
        val l = listOf(
            e(1, 100, SignalStatus.WIN, odd = 2.02),
            e(2, 200, SignalStatus.LOSS, odd = 150.0),
            e(3, 300, SignalStatus.PENDING, odd = 1.45),
        )
        assertEquals(listOf(1L), ids(filterAndSort(l, StatusFilter.ALL, "won", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(2L), ids(filterAndSort(l, StatusFilter.ALL, "LOST", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(3L), ids(filterAndSort(l, StatusFilter.ALL, "active", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(2L), ids(filterAndSort(l, StatusFilter.ALL, "150", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(1L), ids(filterAndSort(l, StatusFilter.ALL, "2.02", SortMode.NEWEST, now, zone)))
        // 12:00:00 + 300 s = 12:05:00
        assertEquals(listOf(3L), ids(filterAndSort(l, StatusFilter.ALL, "12:05:00", SortMode.NEWEST, now, zone)))
        assertEquals(emptyList<Long>(), ids(filterAndSort(l, StatusFilter.ALL, "zzz", SortMode.NEWEST, now, zone)))
    }

    @Test fun search_byCreatedDate() {
        val l = listOf(e(1, 100, created = Instant.parse("2026-11-20T08:30:00Z").toEpochMilli()))
        assertEquals(1, filterAndSort(l, StatusFilter.ALL, "nov 20, 2026", SortMode.NEWEST, now, zone).size)
    }

    @Test fun sort_newestOldest() {
        val l = listOf(e(1, 100, created = 10), e(2, 100, created = 30), e(3, 100, created = 20))
        assertEquals(listOf(2L, 3L, 1L), ids(filterAndSort(l, StatusFilter.ALL, "", SortMode.NEWEST, now, zone)))
        assertEquals(listOf(1L, 3L, 2L), ids(filterAndSort(l, StatusFilter.ALL, "", SortMode.OLDEST, now, zone)))
    }

    @Test fun sort_activeFirst() {
        val l = listOf(
            e(1, 900),                                  // waiting, later
            e(2, 300),                                  // waiting, sooner
            e(3, -5000, SignalStatus.WIN),              // settled, older
            e(4, -2000, SignalStatus.LOSS),             // settled, more recent
            e(5, 30),                                   // ACTIVE (<= 45 s)
            e(6, -20),                                  // ACTIVE, grace (closest to 0 is 6? |-20| < |30|)
        )
        // active (closest to zero first), settled recent-first, waiting soonest-first
        assertEquals(listOf(6L, 5L, 4L, 3L, 2L, 1L), ids(filterAndSort(l, StatusFilter.ALL, "", SortMode.ACTIVE_FIRST, now, zone)))
    }

    @Test fun activeBoundaries() {
        assertTrue(isActiveNow(SignalStatus.PENDING, 45))
        assertFalse(isActiveNow(SignalStatus.PENDING, 46))
        assertTrue(isActiveNow(SignalStatus.PENDING, -44))
        assertFalse(isActiveNow(SignalStatus.PENDING, -45))
        assertFalse(isActiveNow(SignalStatus.WIN, 10))
    }

    @Test fun oddText_likeJs() {
        assertEquals("150", oddText(150.0))
        assertEquals("2.02", oddText(2.02))
        assertEquals("1437.2", oddText(1437.2))
    }

    @Test fun export_json() {
        assertEquals("[]", exportJson(emptyList(), zone))
        val json = exportJson(listOf(e(7, 120, SignalStatus.WIN)), zone)
        assertTrue(json.startsWith("["))
        assertTrue(json.contains("\"id\": 7"))
        assertTrue(json.contains("\"status\": \"win\""))
        assertTrue(json.contains("\"resultTime\": \"12:02:00\""))
        assertTrue(json.contains("\"originalTime\": \"12:00:00\""))
        assertTrue(json.contains("\"resultDate\": \"2026-11-26\""))
    }
}
