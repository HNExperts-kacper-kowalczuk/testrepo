package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.ReviewSuggestionLists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SuggestReviewIngredientsTest {
    private val suggest: SuggestReviewIngredients = SuggestReviewIngredients(
        catalog = FixedCatalogGateway,
        dispatchers = testDispatchers()
    )

    @Test
    fun nearbyListsFuzzyNeighborsForATypo() {
        runBlocking {
            val lists: ReviewSuggestionLists = requireOk(suggest.invoke("NIACINAM1DE", query = ""))
            assertTrue(lists.nearby.any { suggestion -> suggestion.id == "niacinamide" })
            assertEquals("niacinamide", lists.nearby.first().id)
            assertEquals(1, lists.nearby.first().distance)
        }
    }

    @Test
    fun searchQueryFindsCatalogNamesNotAlreadyNearby() {
        runBlocking {
            val lists: ReviewSuggestionLists = requireOk(suggest.invoke("NIACINAM1DE", query = "glyc"))
            assertTrue(lists.nearby.any { suggestion -> suggestion.id == "niacinamide" })
            assertTrue(lists.search.any { suggestion -> suggestion.id == "glycerin" })
            assertTrue(lists.search.none { suggestion -> suggestion.id == "niacinamide" })
        }
    }

    @Test
    fun blankRawTokenIsSearchOnly() {
        runBlocking {
            val lists: ReviewSuggestionLists = requireOk(suggest.invoke("", query = "glyc"))
            assertTrue(lists.nearby.isEmpty())
            assertTrue(lists.search.any { suggestion -> suggestion.id == "glycerin" })
        }
    }

    @Test
    fun catalogFailureIsReturned() {
        val failing: SuggestReviewIngredients = SuggestReviewIngredients(
            catalog = FailingCatalogGateway,
            dispatchers = testDispatchers()
        )
        runBlocking {
            val outcome: Outcome<ReviewSuggestionLists> = failing.invoke("Aqua", query = "")
            assertIs<Outcome.Err>(outcome)
            assertIs<AppFailure.CatalogLoad>(outcome.failure)
        }
    }

    private fun requireOk(outcome: Outcome<ReviewSuggestionLists>): ReviewSuggestionLists {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private fun testDispatchers(): AppDispatchers {
        return AppDispatchers(
            main = Dispatchers.Unconfined,
            computation = Dispatchers.Unconfined,
            io = Dispatchers.Unconfined,
            catalogDatabase = Dispatchers.Unconfined,
            userDatabase = Dispatchers.Unconfined
        )
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

    private object FailingCatalogGateway : CatalogGateway {
        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            return Outcome.Err(AppFailure.CatalogLoad(operation = "catalog.await", detail = "missing"))
        }
    }
}
