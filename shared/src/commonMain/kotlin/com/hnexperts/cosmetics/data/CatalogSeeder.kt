package com.hnexperts.cosmetics.data

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

class CatalogSeeder(
    private val database: CatalogDatabase
) {
    fun seedIfEmpty() {
        val existing = database.catalogDatabaseQueries.selectMeta().executeAsOneOrNull()
        if (existing != null) {
            return
        }
        database.transaction {
            insertMeta()
            FixtureCatalog.ingredients.forEach(::insertIngredient)
            FixtureCatalog.products.forEach(::insertProduct)
        }
    }

    private fun insertMeta() {
        database.catalogDatabaseQueries.upsertMeta(
            catalog_version = FixtureCatalog.CATALOG_VERSION,
            ruleset_version = FixtureCatalog.RULESET_VERSION,
            built_at = "2026-08-17T00:00:00Z",
            region = "EU",
            checksum = "fixture",
            supported_comment_locales = "en,pl"
        )
    }

    private fun insertIngredient(item: FixtureIngredient) {
        database.catalogDatabaseQueries.insertIngredient(
            id = item.ingredient.id,
            inci_name = item.ingredient.inciName,
            cas_numbers = item.ingredient.casNumbers,
            function_tags = item.ingredient.functionTags.joinToString(",")
        )
        for (alias in item.aliases) {
            database.catalogDatabaseQueries.insertAlias(
                alias_normalized = InciNormalizer.normalize(alias),
                ingredient_id = item.ingredient.id
            )
        }
        if (item.commaException) {
            database.catalogDatabaseQueries.insertCommaException(
                phrase_normalized = InciNormalizer.normalize(item.ingredient.inciName),
                ingredient_id = item.ingredient.id
            )
        }
        database.catalogDatabaseQueries.insertHazard(
            ingredient_id = item.hazard.ingredientId,
            danger_level = item.hazard.dangerLevel.name,
            regulatory_tags = item.hazard.regulatoryTags.joinToString(","),
            restriction_json = item.hazard.restrictionJson
        )
        for (comment in item.comments) {
            database.catalogDatabaseQueries.insertComment(
                ingredient_id = item.ingredient.id,
                locale = comment.locale,
                summary = comment.summary,
                detail = comment.detail
            )
        }
    }

    private fun insertProduct(item: FixtureProduct) {
        database.catalogDatabaseQueries.insertProduct(
            id = item.product.id,
            name = item.product.name,
            brand = item.product.brand,
            category = item.product.category,
            inci_raw = item.product.inciRaw,
            inci_hash = item.product.inciRaw.hashCode().toString(),
            usage = item.product.usage,
            source = item.product.source,
            verified = if (item.product.verified) 1 else 0
        )
        for (gtin in item.gtins) {
            database.catalogDatabaseQueries.insertBarcode(
                gtin = gtin.filter { character -> character.isDigit() },
                product_id = item.product.id
            )
        }
    }
}
