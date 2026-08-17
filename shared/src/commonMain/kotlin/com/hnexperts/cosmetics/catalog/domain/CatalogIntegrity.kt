package com.hnexperts.cosmetics.catalog.domain

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.crypto.Sha256

object CatalogIntegrity {
    const val FIXTURE_BUILT_AT: String = "2026-08-17T00:00:00Z"
    const val FIXTURE_REGION: String = "EU"

    fun fingerprint(
        catalogVersion: String,
        rulesetVersion: String,
        builtAt: String,
        region: String,
        ingredientIds: Collection<String>,
        productIds: Collection<String>
    ): String {
        val payload: String = buildString {
            append(catalogVersion)
            append('|')
            append(rulesetVersion)
            append('|')
            append(builtAt)
            append('|')
            append(region)
            append('|')
            append(ingredientIds.sorted().joinToString(","))
            append('|')
            append(productIds.sorted().joinToString(","))
        }
        return Sha256.hex(payload)
    }

    fun fixtureChecksum(): String {
        return fingerprint(
            catalogVersion = FixtureCatalog.CATALOG_VERSION,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = FIXTURE_BUILT_AT,
            region = FIXTURE_REGION,
            ingredientIds = FixtureCatalog.ingredients.map { item -> item.ingredient.id },
            productIds = FixtureCatalog.products.map { item -> item.product.id }
        )
    }

    fun fixtureMeta(): CatalogMeta {
        return CatalogMeta(
            catalogVersion = FixtureCatalog.CATALOG_VERSION,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = FIXTURE_BUILT_AT,
            region = FIXTURE_REGION,
            checksum = fixtureChecksum(),
            supportedCommentLocales = listOf("en", "pl")
        )
    }
}
