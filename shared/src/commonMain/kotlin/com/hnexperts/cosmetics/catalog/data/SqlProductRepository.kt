package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.catalogdb.Product as ProductRow
import kotlinx.coroutines.withContext

class SqlProductRepository(
    private val database: CatalogDatabase,
    private val dispatchers: AppDispatchers
) {
    suspend fun findByGtin(rawGtin: String): Product? {
        val gtin: String = GtinNormalizer.normalize(rawGtin)
        if (gtin.isEmpty()) {
            return null
        }
        return withContext(dispatchers.catalogDatabase) {
            val row = database.catalogDatabaseQueries.selectProductByGtin(gtin).executeAsOneOrNull()
            row?.let(::toProduct)
        }
    }

    suspend fun search(query: String): List<Product> {
        val trimmed: String = query.trim()
        return withContext(dispatchers.catalogDatabase) {
            val rows = if (trimmed.isEmpty()) {
                database.catalogDatabaseQueries.selectAllProducts().executeAsList()
            } else {
                database.catalogDatabaseQueries.searchProducts(trimmed, trimmed).executeAsList()
            }
            rows.map(::toProduct)
        }
    }

    private fun toProduct(row: ProductRow): Product {
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
}
