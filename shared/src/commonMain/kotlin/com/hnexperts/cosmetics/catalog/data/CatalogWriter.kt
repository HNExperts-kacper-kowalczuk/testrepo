package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.application.CatalogDelta
import com.hnexperts.cosmetics.catalog.application.CatalogMutationStore
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.InciIdentity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

class CatalogWriter(
    private val database: CatalogDatabase
) : CatalogMutationStore {
    override fun hasCurrentCatalog(version: String, checksum: String): Boolean {
        val existing = database.catalogDatabaseQueries.selectMeta().executeAsOneOrNull() ?: return false
        return existing.catalog_version == version && existing.checksum == checksum
    }

    override fun isEmpty(): Boolean {
        return database.catalogDatabaseQueries.selectMeta().executeAsOneOrNull() == null
    }

    override fun replaceAll(
        ingredients: List<FixtureIngredient>,
        products: List<FixtureProduct>,
        meta: CatalogMeta
    ) {
        database.transaction {
            clearAll()
            ingredients.forEach(::insertIngredient)
            products.forEach(::insertProduct)
            upsertMeta(meta)
        }
    }

    override fun applyDelta(delta: CatalogDelta) {
        database.transaction {
            delta.ingredients.forEach(::insertIngredient)
            delta.products.forEach(::insertProduct)
            upsertMeta(delta.meta)
        }
    }

    override fun upsertMeta(meta: CatalogMeta) {
        database.catalogDatabaseQueries.upsertMeta(
            catalog_version = meta.catalogVersion,
            ruleset_version = meta.rulesetVersion,
            built_at = meta.builtAt,
            region = meta.region,
            checksum = meta.checksum,
            supported_comment_locales = meta.supportedCommentLocales.joinToString(",")
        )
    }

    override fun clearAll() {
        database.catalogDatabaseQueries.deleteComments()
        database.catalogDatabaseQueries.deleteHazards()
        database.catalogDatabaseQueries.deleteCommaExceptions()
        database.catalogDatabaseQueries.deleteAliases()
        database.catalogDatabaseQueries.deleteBarcodes()
        database.catalogDatabaseQueries.deleteProducts()
        database.catalogDatabaseQueries.deleteIngredients()
        database.catalogDatabaseQueries.deleteMeta()
    }

    fun seedFromFixturesIfNeeded() {
        if (isEmpty()) {
            replaceAll(FixtureCatalog.ingredients, FixtureCatalog.products, CatalogIntegrity.fixtureMeta())
        }
    }

    fun applyProductOverlay(products: List<FixtureProduct>) {
        if (products.isEmpty()) {
            return
        }
        database.transaction {
            products.forEach(::insertProduct)
        }
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
            inci_hash = InciIdentity.hash(item.product.inciRaw),
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
