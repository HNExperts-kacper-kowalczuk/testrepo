package com.hnexperts.cosmetics.catalog.domain

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogIntegrityTest {
    @Test
    fun fixtureChecksumIsStableSha256() {
        val checksum: String = CatalogIntegrity.fixtureChecksum()
        assertEquals(64, checksum.length)
        assertTrue(checksum.all { character -> character in '0'..'9' || character in 'a'..'f' })
        assertEquals(checksum, CatalogIntegrity.fixtureMeta().checksum)
    }

    @Test
    fun fingerprintChangesWhenAnIngredientIsRemoved() {
        val full: String = CatalogIntegrity.fixtureChecksum()
        val reduced: String = CatalogIntegrity.fingerprint(
            catalogVersion = FixtureCatalog.CATALOG_VERSION,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
            region = CatalogIntegrity.FIXTURE_REGION,
            ingredientIds = FixtureCatalog.ingredients.drop(1).map { item -> item.ingredient.id },
            productIds = FixtureCatalog.products.map { item -> item.product.id }
        )
        assertTrue(full != reduced)
    }
}
