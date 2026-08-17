package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase

class SqlProductRepository(
    private val database: CatalogDatabase
) {
    fun findByGtin(rawGtin: String): Product? {
        val gtin: String = GtinNormalizer.normalize(rawGtin)
        if (gtin.isEmpty()) {
            return null
        }
        val row = database.catalogDatabaseQueries.selectProductByGtin(gtin).executeAsOneOrNull()
            ?: return null
        return Product(
            id = row.id,
            name = row.name,
            brand = row.brand,
            category = row.category,
            inciRaw = row.inci_raw,
            usage = row.usage,
            source = row.source,
            verified = row.verified != 0L
        )
    }

    fun findById(id: String): Product? {
        val row = database.catalogDatabaseQueries.selectProductById(id).executeAsOneOrNull() ?: return null
        return Product(
            id = row.id,
            name = row.name,
            brand = row.brand,
            category = row.category,
            inciRaw = row.inci_raw,
            usage = row.usage,
            source = row.source,
            verified = row.verified != 0L
        )
    }

    fun search(query: String): List<Product> {
        val trimmed: String = query.trim()
        if (trimmed.isEmpty()) {
            return database.catalogDatabaseQueries.selectAllProducts().executeAsList().map { row ->
                Product(
                    id = row.id,
                    name = row.name,
                    brand = row.brand,
                    category = row.category,
                    inciRaw = row.inci_raw,
                    usage = row.usage,
                    source = row.source,
                    verified = row.verified != 0L
                )
            }
        }
        return database.catalogDatabaseQueries.searchProducts(trimmed, trimmed).executeAsList().map { row ->
            Product(
                id = row.id,
                name = row.name,
                brand = row.brand,
                category = row.category,
                inciRaw = row.inci_raw,
                usage = row.usage,
                source = row.source,
                verified = row.verified != 0L
            )
        }
    }
}
