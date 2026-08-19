package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog

/**
 * Merges CosIng/OBF ingest dumps with curated fixtures. Fixture ingredient ids
 * and GTINs win so editorial comments and demo barcodes stay stable.
 */
object ShippedCatalogMerger {
    const val CATALOG_VERSION: String = "2026.08-eu-pl"

    fun merge(
        ingestedIngredients: CosingIngredientDump?,
        ingestedProducts: ObfProductDump?,
        maxProducts: Int = Int.MAX_VALUE,
        builtAt: String
    ): CatalogBuild {
        val ingredientsDump = CosingIngredientDump(
            region = "EU",
            catalogVersion = CATALOG_VERSION,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = builtAt,
            ingredients = mergeIngredients(ingestedIngredients)
        )
        val productsDump = ObfProductDump(
            region = "EU",
            products = mergeProducts(ingestedProducts, maxProducts)
        )
        return CatalogBuilder.build(ingredientsDump, productsDump)
    }

    private fun mergeIngredients(ingested: CosingIngredientDump?): List<CosingIngredientRecord> {
        val byId: LinkedHashMap<String, CosingIngredientRecord> = LinkedHashMap()
        if (ingested != null) {
            for (record in ingested.ingredients) {
                byId[record.id] = record
            }
        }
        val fixtures: CosingIngredientDump = CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        for (record in fixtures.ingredients) {
            byId[record.id] = record
        }
        return byId.values.toList()
    }

    private fun mergeProducts(ingested: ObfProductDump?, maxProducts: Int): List<ObfProductRecord> {
        val fixtures: ObfProductDump = CatalogSourceCodec.parseProducts(CatalogSourceCodec.encodeProducts())
        val fixtureGtins: Set<String> = fixtures.products
            .flatMap { record -> record.gtins }
            .map(::digitsOnly)
            .filter { gtin -> gtin.isNotEmpty() }
            .toSet()
        val extra: MutableList<ObfProductRecord> = mutableListOf()
        if (ingested != null) {
            for (record in ingested.products) {
                if (extra.size >= extraSlots(maxProducts, fixtures.products.size)) {
                    break
                }
                val overlap: Boolean = record.gtins.map(::digitsOnly).any { gtin -> fixtureGtins.contains(gtin) }
                if (!overlap) {
                    extra.add(record)
                }
            }
        }
        return fixtures.products + extra
    }

    private fun extraSlots(maxProducts: Int, fixtureCount: Int): Int {
        val remaining: Int = maxProducts - fixtureCount
        return if (remaining < 0) 0 else remaining
    }

    private fun digitsOnly(raw: String): String {
        return raw.filter { character -> character.isDigit() }
    }
}
