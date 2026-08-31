package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.FuzzyHit
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FuzzyAutoAcceptTest {
    @Test
    fun acceptsUniqueSingleEditOnLongToken() {
        val hit: FuzzyHit = hit(distance = 1, unique = true)
        assertTrue(FuzzyAutoAccept.shouldAccept(hit, normalizedLength = 11))
        assertEquals(FuzzyDecision.AUTO_ACCEPTED, FuzzyAutoAccept.decision(hit, 11))
    }

    @Test
    fun rejectsShortTokensEvenWhenUnique() {
        val hit: FuzzyHit = hit(distance = 1, unique = true)
        assertFalse(FuzzyAutoAccept.shouldAccept(hit, normalizedLength = 7))
        assertEquals(FuzzyDecision.PENDING, FuzzyAutoAccept.decision(hit, 7))
    }

    @Test
    fun rejectsAmbiguousHits() {
        val hit: FuzzyHit = hit(distance = 1, unique = false)
        assertFalse(FuzzyAutoAccept.shouldAccept(hit, normalizedLength = 11))
        assertEquals(FuzzyDecision.PENDING, FuzzyAutoAccept.decision(hit, 11))
    }

    @Test
    fun rejectsDistanceThreeEvenWhenUnique() {
        val hit: FuzzyHit = hit(distance = 3, unique = true)
        assertFalse(FuzzyAutoAccept.shouldAccept(hit, normalizedLength = 11))
        assertEquals(FuzzyDecision.PENDING, FuzzyAutoAccept.decision(hit, 11))
    }

    @Test
    fun acceptsUniqueDistanceTwoOnLongToken() {
        val hit: FuzzyHit = hit(distance = 2, unique = true)
        assertTrue(FuzzyAutoAccept.shouldAccept(hit, normalizedLength = 11))
        assertEquals(FuzzyDecision.AUTO_ACCEPTED, FuzzyAutoAccept.decision(hit, 11))
    }

    private fun hit(distance: Int, unique: Boolean): FuzzyHit {
        return FuzzyHit(
            ingredient = Ingredient(id = "niacinamide", inciName = "Niacinamide", casNumbers = null, functionTags = emptyList()),
            distance = distance,
            unique = unique
        )
    }
}
