package com.hnexperts.cosmetics.catalog.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PregnancyCautionIndexTest {
    @Test
    fun retinalIsTagged() {
        assertEquals(listOf(PregnancyCautionIndex.TAG), PregnancyCautionIndex.tagsFor("RETINAL"))
    }

    @Test
    fun glycerinIsNotTagged() {
        assertTrue(PregnancyCautionIndex.tagsFor("Glycerin").isEmpty())
    }
}
