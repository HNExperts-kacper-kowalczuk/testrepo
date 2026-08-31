package com.hnexperts.cosmetics.scanning.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class InciRawEditorTest {
    @Test
    fun replacesTheIndexedTokenAndKeepsNeighbors() {
        val joined: String = InciRawEditor.replaceAt(
            tokens = listOf("Aqua", "CompletelyUnknownStuff", "Glycerin"),
            index = 1,
            replacement = "Niacinamide"
        )
        assertEquals("Aqua, Niacinamide, Glycerin", joined)
    }

    @Test
    fun outOfRangeIndexLeavesTheListUnchanged() {
        val joined: String = InciRawEditor.replaceAt(
            tokens = listOf("Aqua", "Glycerin"),
            index = 4,
            replacement = "Niacinamide"
        )
        assertEquals("Aqua, Glycerin", joined)
    }
}
