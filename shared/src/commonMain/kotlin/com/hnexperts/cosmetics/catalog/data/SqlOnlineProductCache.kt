package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.domain.CachedOnlineProduct
import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import kotlinx.coroutines.withContext

class SqlOnlineProductCache(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : OnlineProductCache {
    override suspend fun find(gtin: String): Outcome<CachedOnlineProduct?> {
        val normalized: String = GtinNormalizer.normalize(gtin)
        if (normalized.isEmpty()) {
            return Outcome.Ok(null)
        }
        return FailureCatcher.database("cache.findByGtin") {
            withContext(dispatchers.userDatabase) {
                val row = database.userDatabaseQueries.selectCachedProduct(normalized).executeAsOneOrNull()
                    ?: return@withContext null
                CachedOnlineProduct(
                    gtin = row.gtin,
                    name = row.name,
                    brand = row.brand,
                    inciRaw = row.inci_raw,
                    usage = row.usage,
                    source = row.source,
                    cachedAt = row.cached_at
                )
            }
        }
    }

    override suspend fun put(product: CachedOnlineProduct): Outcome<Unit> {
        return FailureCatcher.database("cache.put") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.upsertCachedProduct(
                    gtin = GtinNormalizer.normalize(product.gtin),
                    name = product.name,
                    brand = product.brand,
                    inci_raw = product.inciRaw,
                    usage = product.usage,
                    source = product.source,
                    cached_at = product.cachedAt
                )
            }
        }
    }
}
