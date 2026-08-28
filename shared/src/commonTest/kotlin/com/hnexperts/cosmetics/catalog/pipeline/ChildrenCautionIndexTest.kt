package com.hnexperts.cosmetics.catalog.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChildrenCautionIndexTest {
    @Test
    fun methylSalicylateIsTagged() {
        assertEquals(listOf(ChildrenCautionIndex.TAG), ChildrenCautionIndex.tagsFor("Methyl Salicylate"))
    }

    @Test
    fun glycerinIsNotTagged() {
        assertTrue(ChildrenCautionIndex.tagsFor("Glycerin").isEmpty())
    }
}
