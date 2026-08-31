package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class ReplaceUnmatchedIngredientTest {
    private val replace: ReplaceUnmatchedIngredient = ReplaceUnmatchedIngredient(FixedCatalogGateway)

    @Test
    fun replacesUnknownTokenUsingTheSameTokenizer() {
        runBlocking {
            val joined: String = requireOk(
                replace.invoke("Aqua, CompletelyUnknownStuff, Glycerin", listIndex = 1, catalogName = "Niacinamide")
            )
            assertEquals("AQUA, Niacinamide, GLYCERIN", joined)
        }
    }

    private fun requireOk(outcome: Outcome<String>): String {
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
