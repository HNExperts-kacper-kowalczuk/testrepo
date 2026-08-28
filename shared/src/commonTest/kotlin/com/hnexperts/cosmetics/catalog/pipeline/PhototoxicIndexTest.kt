package com.hnexperts.cosmetics.catalog.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhototoxicIndexTest {
    @Test
    fun bergamotPeelOilIsTagged() {
        assertEquals(
            listOf(PhototoxicIndex.TAG),
            PhototoxicIndex.tagsFor("Citrus Aurantium Bergamia Peel Oil")
        )
    }

    @Test
    fun glycerinIsNotTagged() {
        assertTrue(PhototoxicIndex.tagsFor("Glycerin").isEmpty())
    }
}
