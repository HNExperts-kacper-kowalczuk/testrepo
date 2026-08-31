package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class OpenTypedIngredientReviewTest {
    private val reviewSession: IngredientReviewSession = IngredientReviewSession()
    private val openReview: OpenTypedIngredientReview = OpenTypedIngredientReview(
        prepareReview = PrepareIngredientReview(FixedCatalogGateway),
        reviewSession = reviewSession
    )

    @Test
    fun exactListIsReadyWithoutConfirm() {
        runBlocking {
            val outcome: TypedIngredientReview = requireOk(openReview.invoke("Aqua, Glycerin", ProductUsage.LEAVE_ON))
            val ready: TypedIngredientReview.Ready = assertIs(outcome)
            assertTrue(ready.inciRaw.contains("Aqua"))
            assertTrue(ready.inciRaw.contains("Glycerin"))
            assertEquals(ProductUsage.LEAVE_ON, ready.usage)
            assertEquals(null, reviewSession.current())
        }
    }

    @Test
    fun autoFilledTypoOpensConfirm() {
        runBlocking {
            val outcome: TypedIngredientReview = requireOk(
                openReview.invoke("Aqua, NIACINAM1DE", ProductUsage.RINSE_OFF)
            )
            val confirm: TypedIngredientReview.Confirm = assertIs(outcome)
            val draft: IngredientReviewDraft = confirm.draft
            assertTrue(draft.needsReview())
            assertTrue(draft.hasAutoFilledFuzzy())
            assertEquals(ProductUsage.RINSE_OFF, draft.usage)
            assertEquals("manual", draft.source)
            assertEquals("Niacinamide", draft.tokens[1].suggestedName)
            assertEquals(draft, reviewSession.current())
        }
    }

    @Test
    fun unknownTokenOpensConfirm() {
        runBlocking {
            val outcome: TypedIngredientReview = requireOk(
                openReview.invoke("CompletelyUnknownStuff", ProductUsage.LEAVE_ON)
            )
            val confirm: TypedIngredientReview.Confirm = assertIs(outcome)
            assertTrue(confirm.draft.needsReview())
            assertFalse(confirm.draft.hasPendingFuzzy())
        }
    }

    @Test
    fun emptyTextIsOcrFailure() {
        runBlocking {
            val outcome: Outcome<TypedIngredientReview> = openReview.invoke("   ", ProductUsage.LEAVE_ON)
            assertIs<Outcome.Err>(outcome)
            assertIs<AppFailure.Ocr>(outcome.failure)
        }
    }

    @Test
    fun pendingShortTypoOpensConfirm() {
        runBlocking {
            val outcome: TypedIngredientReview = requireOk(openReview.invoke("RET1NOL", ProductUsage.LEAVE_ON))
            val confirm: TypedIngredientReview.Confirm = assertIs(outcome)
            assertEquals(FuzzyDecision.PENDING, confirm.draft.tokens[0].fuzzyDecision)
        }
    }

    private fun requireOk(outcome: Outcome<TypedIngredientReview>): TypedIngredientReview {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private object FixedCatalogGateway : CatalogGateway {
        private val index: CatalogIndex = CatalogIndex.assemble(
            CatalogSnapshot(
                meta = CatalogIntegrity.fixtureMeta(),
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
