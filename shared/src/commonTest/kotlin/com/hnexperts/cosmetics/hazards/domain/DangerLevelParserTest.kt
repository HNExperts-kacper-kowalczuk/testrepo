package com.hnexperts.cosmetics.hazards.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DangerLevelParserTest {
    @Test
    fun parsesKnownLevel() {
        assertEquals(DangerLevel.HIGH, DangerLevelParser.parse("HIGH"))
    }

    @Test
    fun rejectsUnknownLevel() {
        assertFailsWith<IllegalArgumentException> {
            DangerLevelParser.parse("TOXIC")
        }
    }
}
