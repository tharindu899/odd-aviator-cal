package com.aviator.hoc.domain

import com.aviator.hoc.core.Hms
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePreviewTest {
    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2026-11-26T12:00:00Z").toEpochMilli()

    @Test fun standardExample() {
        val p = previewFor("2.02x 21:31:22", now, zone)
        assertNotNull(p)
        assertEquals(Hms(21, 33, 24), p!!.resultTime)
        assertEquals(Hms(21, 32, 39), p.window.open)
        assertEquals(Hms(21, 34, 9), p.window.close)
        assertFalse(p.nextDay)
        assertFalse(p.alreadyPassed)
    }

    @Test fun crossesMidnight_showsPlusOneDay() {
        val p = previewFor("2.02x 23:59:00", now, zone)!!
        assertEquals(Hms(0, 1, 2), p.resultTime)
        assertTrue(p.nextDay)
    }

    @Test fun highOdd_wrapsToNextDay() {
        val p = previewFor("150x 23:34:00", now, zone)!!      // 23:34:00 + 2:29:00
        assertEquals(Hms(2, 3, 0), p.resultTime)
        assertTrue(p.nextDay)
    }

    @Test fun alreadyPassed_whenWindowIsOver() {
        assertTrue(previewFor("2.02x 10:00:00", now, zone)!!.alreadyPassed)
        assertFalse(previewFor("2.02x 12:00:00", now, zone)!!.alreadyPassed)
    }

    @Test fun invalidInput_isNull() {
        assertNull(previewFor("", now, zone))
        assertNull(previewFor("2.02x", now, zone))
        assertNull(previewFor("2.02x 99:00:00", now, zone))
    }

    @Test fun normalizePasted_insertsMissingSpace_andTrims() {
        assertEquals("2.02x 21:31:22", normalizePasted("2.02x21:31:22"))
        assertEquals("150x 23:34:00", normalizePasted("  150x 23:34:00 \n"))
        assertEquals("1,437.20x 06:31:56", normalizePasted("1,437.20x06:31:56"))
    }
}
