package com.hnexperts.cosmetics.catalog.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MicroplasticIndexTest {
    @Test
    fun polyethyleneIsTagged() {
        assertEquals(listOf(MicroplasticIndex.TAG), MicroplasticIndex.tagsFor("Polyethylene"))
        assertEquals(listOf(MicroplasticIndex.TAG), MicroplasticIndex.tagsFor("POLYETHYLENE"))
    }

    @Test
    fun glycerinIsNotTagged() {
        assertTrue(MicroplasticIndex.tagsFor("Glycerin").isEmpty())
    }
}
