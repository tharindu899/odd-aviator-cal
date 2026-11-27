package com.aviator.hoc.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateLogicTest {
    @Test
    fun tagToVersionCode() {
        assertEquals(17, versionCodeFromTag("build-17"))
        assertEquals(3, versionCodeFromTag("v3"))
        assertNull(versionCodeFromTag("latest"))
        assertNull(versionCodeFromTag(null))
    }

    @Test
    fun newerOnlyWhenStrictlyGreater() {
        assertTrue(isNewer(18, 17))
        assertFalse(isNewer(17, 17))
        assertFalse(isNewer(16, 17))
        assertFalse(isNewer(null, 1))
    }

    @Test
    fun notesAreTrimmedToFewLines() {
        val n = shortNotes("a\n\n  b  \nc\nd\ne", 3)
        assertEquals("a\nb\nc", n)
        assertEquals("", shortNotes(null))
    }
}
