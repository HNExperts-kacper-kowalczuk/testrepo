package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
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
