package com.hnexperts.cosmetics.ingredients.domain

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientMatcherConcurrencyTest {
    private val matcher: IngredientMatcher = IngredientMatcher(
        ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
        aliases = FixtureCatalog.aliasMap(),
        commaExceptions = FixtureCatalog.commaExceptions()
    )

    @Test
    fun concurrentMatchingPreservesOrderAndResults() {
        val inci: String = FixtureCatalog.products
            .joinToString(", ") { item -> item.product.inciRaw }
        val sequential: List<IngredientRef> = matcher.matchList(inci)
        val concurrent: List<IngredientRef> = runBlocking { matcher.matchListConcurrently(inci) }
        assertEquals(sequential.map { ref -> ref.id }, concurrent.map { ref -> ref.id })
        assertEquals(sequential.map { ref -> ref.matchedBy }, concurrent.map { ref -> ref.matchedBy })
    }
}
