package com.hnexperts.cosmetics.scanning.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewMatchSuggestionsTest {
    @Test
    fun searchOmitsIdsAlreadyInNearby() {
        val nearby: List<IngredientSuggestion> = listOf(
            IngredientSuggestion(id = "niacinamide", inciName = "Niacinamide", distance = 1)
        )
        val search: List<IngredientSuggestion> = listOf(
            IngredientSuggestion(id = "niacinamide", inciName = "Niacinamide", distance = null),
            IngredientSuggestion(id = "glycerin", inciName = "Glycerin", distance = null)
        )
        val merged: ReviewSuggestionLists = ReviewMatchSuggestions.merge(nearby, search)
        assertEquals(listOf("niacinamide"), merged.nearby.map { suggestion -> suggestion.id })
        assertEquals(listOf("glycerin"), merged.search.map { suggestion -> suggestion.id })
    }

    @Test
    fun emptyNearbyKeepsSearchOrder() {
        val search: List<IngredientSuggestion> = listOf(
            IngredientSuggestion(id = "aqua", inciName = "Aqua", distance = null),
            IngredientSuggestion(id = "glycerin", inciName = "Glycerin", distance = null)
        )
        val merged: ReviewSuggestionLists = ReviewMatchSuggestions.merge(emptyList(), search)
        assertEquals(emptyList(), merged.nearby)
        assertEquals(listOf("aqua", "glycerin"), merged.search.map { suggestion -> suggestion.id })
    }
}
