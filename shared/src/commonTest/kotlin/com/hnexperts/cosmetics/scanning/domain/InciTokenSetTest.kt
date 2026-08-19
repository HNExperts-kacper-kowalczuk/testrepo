package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InciTokenSetTest {
    @Test
    fun orderAndSpacingDoNotMatter() {
        assertTrue(
            InciTokenSet.equal(
                "Aqua, Glycerin, Niacinamide",
                "glycerin, aqua, niacinamide"
            )
        )
    }

    @Test
    fun extraTokenIsAMismatch() {
        assertFalse(
            InciTokenSet.equal(
                "Aqua, Glycerin",
                "Aqua, Glycerin, Parfum"
            )
        )
    }
}

class ReviewDraftMergerTest {
    @Test
    fun dropsNormalizedDuplicatesAndKeepsNewNames() {
        val first = draft("Aqua", "Glycerin")
        val second = draft("GLYCERIN", "Panthenol")
        val merged: IngredientReviewDraft = ReviewDraftMerger.merge(first, second)
        val names: List<String> = merged.tokens.map { token -> token.inciName() }
        assertEquals(listOf("Aqua", "Glycerin", "Panthenol"), names)
    }

    private fun draft(vararg names: String): IngredientReviewDraft {
        val tokens: List<ReviewToken> = names.mapIndexed { index, name ->
            ReviewToken(
                key = index.toLong() + 1L,
                rawText = name,
                suggestedName = name,
                matchedIngredientId = null,
                matchMethod = MatchMethod.UNMATCHED,
                fuzzyDecision = FuzzyDecision.NOT_APPLICABLE
            )
        }
        return IngredientReviewDraft(rawText = names.joinToString(", "), tokens = tokens, nextKey = tokens.size + 1L)
    }
}
