package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.application.CatalogDelta
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogPipelineTest {
    @Test
    fun roundTripMatchesFixtureFingerprint() {
        val ingredientsDump: CosingIngredientDump =
            CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        val productsDump: ObfProductDump =
            CatalogSourceCodec.parseProducts(CatalogSourceCodec.encodeProducts())
        val build: CatalogBuild = CatalogBuilder.build(ingredientsDump, productsDump)
        assertEquals(CatalogIntegrity.fixtureChecksum(), build.meta.checksum)
        assertEquals(FixtureCatalog.ingredients.size, build.ingredients.size)
        assertEquals(FixtureCatalog.products.size, build.products.size)
        assertEquals(FixtureCatalog.CATALOG_VERSION, build.manifest.catalogVersion)
    }

    @Test
    fun deltaRoundTripKeepsAddedProduct() {
        val extra: FixtureProduct = FixtureCatalog.products.first()
        val delta = CatalogDelta(
            fromCatalogVersion = FixtureCatalog.CATALOG_VERSION,
            meta = CatalogIntegrity.fixtureMeta().copy(catalogVersion = "2026.09-hosted"),
            ingredients = emptyList(),
            products = listOf(extra)
        )
        val decoded = CatalogSourceCodec.decodeDelta(CatalogSourceCodec.encodeDelta(delta))
        assertEquals(delta.fromCatalogVersion, decoded.fromCatalogVersion)
        assertEquals(delta.meta.catalogVersion, decoded.meta.catalogVersion)
        assertEquals(extra.product.id, decoded.products.single().product.id)
        assertEquals(extra.product.inciRaw, decoded.products.single().product.inciRaw)
    }

    @Test
    fun highAndProhibitedIngredientsHaveEnAndPlComments() {
        val dump: CosingIngredientDump =
            CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        val errors: List<String> = CatalogCommentValidator().validate(dump)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun validatorFlagsMissingPolishOnProhibited() {
        val dump: CosingIngredientDump =
            CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        val stripped: CosingIngredientDump = dump.copy(
            ingredients = dump.ingredients.map { ingredient ->
                if (ingredient.id == "formaldehyde") {
                    ingredient.copy(comments = ingredient.comments.filter { comment -> comment.locale == "en" })
                } else {
                    ingredient
                }
            }
        )
        val errors: List<String> = CatalogCommentValidator().validate(stripped)
        assertTrue(errors.any { message -> message.contains("formaldehyde") && message.contains("pl") })
    }
}
