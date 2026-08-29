package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.overlay.PolishProductOverlay

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
        val polish: List<ObfProductRecord> = PolishProductOverlay.records
        val reservedGtins: Set<String> = gtinSet(fixtures.products + polish)
        val reservedCount: Int = fixtures.products.size + polish.size
        val extra: MutableList<ObfProductRecord> = mutableListOf()
        if (ingested != null) {
            for (record in ingested.products) {
                if (extra.size >= extraSlots(maxProducts, reservedCount)) {
                    break
                }
                val overlap: Boolean = record.gtins.map(::digitsOnly).any { gtin -> reservedGtins.contains(gtin) }
                if (!overlap) {
                    extra.add(record)
                }
            }
        }
        return fixtures.products + polish + extra
    }

    private fun gtinSet(records: List<ObfProductRecord>): Set<String> {
        return records
            .flatMap { record -> record.gtins }
            .map(::digitsOnly)
            .filter { gtin -> gtin.isNotEmpty() }
            .toSet()
    }

    private fun extraSlots(maxProducts: Int, reservedCount: Int): Int {
        val remaining: Int = maxProducts - reservedCount
        return if (remaining < 0) 0 else remaining
    }

    private fun digitsOnly(raw: String): String {
        return raw.filter { character -> character.isDigit() }
    }
}
