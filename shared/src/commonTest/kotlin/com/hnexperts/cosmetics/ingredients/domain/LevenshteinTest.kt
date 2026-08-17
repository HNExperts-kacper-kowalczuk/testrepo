package com.hnexperts.cosmetics.ingredients.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LevenshteinTest {
    @Test
    fun identicalStringsHaveZeroDistance() {
        assertEquals(0, Levenshtein.distance("NIACINAMIDE", "NIACINAMIDE"))
    }

    @Test
    fun singleSubstitutionIsOne() {
        assertEquals(1, Levenshtein.distance("NIACINAMIDE", "NIACINAM1DE"))
    }
}
