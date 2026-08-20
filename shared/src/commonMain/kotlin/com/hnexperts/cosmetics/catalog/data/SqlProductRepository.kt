package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.catalogdb.Product as ProductRow
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import kotlinx.coroutines.withContext

class SqlProductRepository(
    private val database: CatalogDatabase,
    private val dispatchers: AppDispatchers
) : ProductRepository {
    override suspend fun findByGtin(rawGtin: String): Outcome<Product?> {
        val gtin: String = GtinNormalizer.normalize(rawGtin)
        if (gtin.isEmpty()) {
            return Outcome.Ok(null)
        }
        return FailureCatcher.database("catalog.findByGtin") {
            withContext(dispatchers.catalogDatabase) {
                val row = database.catalogDatabaseQueries.selectProductByGtin(gtin).executeAsOneOrNull()
                row?.let(::toProduct)
            }
        }
    }

    override suspend fun search(query: String): Outcome<List<Product>> {
        val trimmed: String = query.trim()
        return FailureCatcher.database("catalog.search") {
            withContext(dispatchers.catalogDatabase) {
                val rows = if (trimmed.isEmpty()) {
                    emptyList()
                } else {
                    database.catalogDatabaseQueries.searchProducts(trimmed, trimmed).executeAsList()
                }
                rows.map(::toProduct)
            }
        }
    }

    override suspend fun findByCategory(category: String, limit: Int): Outcome<List<Product>> {
        val trimmed: String = category.trim()
        if (trimmed.isEmpty() || limit <= 0) {
            return Outcome.Ok(emptyList())
        }
        return FailureCatcher.database("catalog.findByCategory") {
            withContext(dispatchers.catalogDatabase) {
                database.catalogDatabaseQueries
                    .selectProductsByCategory(trimmed, limit.toLong())
                    .executeAsList()
                    .map(::toProduct)
            }
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
