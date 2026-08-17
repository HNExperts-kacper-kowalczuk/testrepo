package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class PrepareIngredientReviewTest {
    private val prepareReview: PrepareIngredientReview = PrepareIngredientReview(FixedCatalogGateway)

    @Test
    fun emptyTextIsOcrFailure() = runBlocking {
        val outcome: Outcome<IngredientReviewDraft> = prepareReview.invoke("   ")
        assertIs<Outcome.Err>(outcome)
        assertIs<AppFailure.Ocr>(outcome.failure)
        assertEquals("ocr.empty", outcome.failure.operation)
    }

    @Test
    fun keepsExactTokensAndMarksFuzzyAsPending() = runBlocking {
        val draft: IngredientReviewDraft = requireOk(
            prepareReview.invoke("Aqua, NIACINAM1DE, CompletelyUnknownStuff")
        )
        assertEquals(3, draft.tokens.size)
        assertEquals(MatchMethod.EXACT, draft.tokens[0].matchMethod)
        assertEquals("AQUA", draft.tokens[0].rawText)
        assertEquals("Aqua", draft.tokens[0].suggestedName)
        assertEquals(MatchMethod.FUZZY, draft.tokens[1].matchMethod)
        assertEquals(FuzzyDecision.PENDING, draft.tokens[1].fuzzyDecision)
        assertEquals("NIACINAM1DE", draft.tokens[1].rawText)
        assertEquals("Niacinamide", draft.tokens[1].suggestedName)
        assertEquals(MatchMethod.UNMATCHED, draft.tokens[2].matchMethod)
        assertTrue(draft.hasPendingFuzzy())
        assertFalse(draft.toInciRaw().contains("Niacinamide"))
    }

    private fun requireOk(outcome: Outcome<IngredientReviewDraft>): IngredientReviewDraft {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private object FixedCatalogGateway : CatalogGateway {
        private val index: CatalogIndex = CatalogIndex.assemble(
            CatalogSnapshot(
                rulesetVersion = FixtureCatalog.RULESET_VERSION,
                ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
                aliases = FixtureCatalog.aliasMap(),
                commaExceptions = FixtureCatalog.commaExceptions(),
                hazards = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.hazard },
                comments = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.comments }
            )
        )

        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            return Outcome.Ok(index)
        }
    }
}
