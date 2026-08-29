package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.overlay.PolishProductOverlay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShippedCatalogMergerTest {
    @Test
    fun fixtureIngredientsWinOnIdConflict() {
        val ingested = CosingIngredientDump(
            region = "EU",
            catalogVersion = "ingest",
            rulesetVersion = "ingest",
            builtAt = "2026-01-01T00:00:00Z",
            ingredients = listOf(
                CosingIngredientRecord(
                    id = FixtureCatalog.ingredients.first().ingredient.id,
                    inciName = "SHOULD NOT WIN",
                    dangerLevel = "LOW",
                    comments = emptyList()
                )
            )
        )
        val build = ShippedCatalogMerger.merge(
            ingestedIngredients = ingested,
            ingestedProducts = ObfProductDump(region = "EU", products = emptyList()),
            builtAt = "2026-08-19T00:00:00Z"
        )
        val overwritten = build.ingredients.first { item -> item.ingredient.id == ingested.ingredients.first().id }
        assertEquals(FixtureCatalog.ingredients.first().ingredient.inciName, overwritten.ingredient.inciName)
    }

    @Test
    fun fixtureGtinsAreKeptAndExtrasFillUpToTheCap() {
        val extra = ObfProductRecord(
            id = "obf-extra",
            name = "Extra Cream",
            inciRaw = "Aqua, Glycerin, Panthenol, Niacinamide",
            gtins = listOf("4000000999999")
        )
        val colliding = ObfProductRecord(
            id = "obf-collide",
            name = "Colliding",
            inciRaw = "Aqua, Glycerin, Panthenol, Niacinamide",
            gtins = listOf(FixtureCatalog.products.first().gtins.first())
        )
        val build = ShippedCatalogMerger.merge(
            ingestedIngredients = null,
            ingestedProducts = ObfProductDump(region = "EU", products = listOf(colliding, extra)),
            maxProducts = FixtureCatalog.products.size + PolishProductOverlay.products.size + 1,
            builtAt = "2026-08-19T00:00:00Z"
        )
        assertEquals(FixtureCatalog.products.size + PolishProductOverlay.products.size + 1, build.products.size)
        assertTrue(build.products.any { item -> item.product.id == "obf-extra" })
        assertTrue(build.products.any { item -> item.gtins.contains("5901887019367") })
        assertTrue(build.products.none { item -> item.product.id == "obf-collide" })
    }
}
