package com.hnexperts.cosmetics.catalog.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimalDerivedIndexTest {
    @Test
    fun carmineAndBeeswaxAreTagged() {
        assertEquals(listOf(AnimalDerivedIndex.TAG), AnimalDerivedIndex.tagsFor("Carmine"))
        assertEquals(listOf(AnimalDerivedIndex.TAG), AnimalDerivedIndex.tagsFor("CERA ALBA"))
        assertEquals(listOf(AnimalDerivedIndex.TAG), AnimalDerivedIndex.tagsFor("CI 75470"))
    }

    @Test
    fun glycerinAndSqualaneAreNotTagged() {
        assertTrue(AnimalDerivedIndex.tagsFor("Glycerin").isEmpty())
        assertTrue(AnimalDerivedIndex.tagsFor("Squalane").isEmpty())
    }
}
