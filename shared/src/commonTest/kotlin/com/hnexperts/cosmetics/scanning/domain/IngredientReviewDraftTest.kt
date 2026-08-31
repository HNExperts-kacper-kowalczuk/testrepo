package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IngredientReviewDraftTest {
    @Test
    fun inciNameKeepsRawUntilFuzzyIsAccepted() {
        val pending: ReviewToken = token(method = MatchMethod.FUZZY, decision = FuzzyDecision.PENDING)
        assertEquals("NIACINAM1DE", pending.inciName())
        val accepted: ReviewToken = pending.copy(fuzzyDecision = FuzzyDecision.ACCEPTED)
        assertEquals("Niacinamide", accepted.inciName())
        val autoAccepted: ReviewToken = pending.copy(fuzzyDecision = FuzzyDecision.AUTO_ACCEPTED)
        assertEquals("Niacinamide", autoAccepted.inciName())
        val rejected: ReviewToken = pending.copy(fuzzyDecision = FuzzyDecision.REJECTED)
        assertEquals("NIACINAM1DE", rejected.inciName())
    }

    @Test
    fun exactMatchUsesSuggestedName() {
        val exact: ReviewToken = token(method = MatchMethod.EXACT, decision = FuzzyDecision.NOT_APPLICABLE)
        assertEquals("Niacinamide", exact.inciName())
    }

    @Test
    fun toInciRawSkipsBlankAndPendingUsesRaw() {
        val draft = IngredientReviewDraft(
            rawText = "NIACINAM1DE,  ",
            tokens = listOf(
                token(key = 1L, method = MatchMethod.FUZZY, decision = FuzzyDecision.PENDING),
                ReviewToken(
                    key = 2L,
                    rawText = "  ",
                    suggestedName = "",
                    matchedIngredientId = null,
                    matchMethod = MatchMethod.UNMATCHED,
                    fuzzyDecision = FuzzyDecision.NOT_APPLICABLE
                )
            ),
            nextKey = 3L
        )
        assertTrue(draft.hasPendingFuzzy())
        assertEquals("NIACINAM1DE", draft.toInciRaw())
        assertFalse(draft.copy(tokens = listOf(draft.tokens[0].copy(fuzzyDecision = FuzzyDecision.ACCEPTED))).hasPendingFuzzy())
        val autoFilled: IngredientReviewDraft = draft.copy(
            tokens = listOf(draft.tokens[0].copy(fuzzyDecision = FuzzyDecision.AUTO_ACCEPTED))
        )
        assertFalse(autoFilled.hasPendingFuzzy())
        assertTrue(autoFilled.hasAutoFilledFuzzy())
        assertEquals("Niacinamide", autoFilled.toInciRaw())
    }

    private fun token(
        key: Long = 1L,
        method: MatchMethod,
        decision: FuzzyDecision
    ): ReviewToken {
        return ReviewToken(
            key = key,
            rawText = "NIACINAM1DE",
            suggestedName = "Niacinamide",
            matchedIngredientId = "niacinamide",
            matchMethod = method,
            fuzzyDecision = decision
        )
    }
}
