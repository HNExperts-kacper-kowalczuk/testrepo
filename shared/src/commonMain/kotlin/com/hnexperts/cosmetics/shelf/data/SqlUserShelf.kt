package com.hnexperts.cosmetics.shelf.data

import com.hnexperts.cosmetics.catalog.domain.InciIdentity
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.data.userdb.User_shelf
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.shelf.domain.UserShelf
import kotlin.time.Clock
import kotlinx.coroutines.withContext

class SqlUserShelf(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : UserShelf {
    override suspend fun all(): Outcome<List<ShelfItem>> {
        return FailureCatcher.database("shelf.all") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.selectShelf().executeAsList().map(::toItem)
            }
        }
    }

    override suspend fun contains(shelfKey: String): Outcome<Boolean> {
        return FailureCatcher.database("shelf.contains") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.selectShelfByKey(shelfKey).executeAsOneOrNull() != null
            }
        }
    }

    override suspend fun save(item: ShelfItem): Outcome<Unit> {
        return FailureCatcher.database("shelf.save") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.upsertShelf(
                    shelf_key = item.shelfKey,
                    product_id = item.productId,
                    gtin = item.gtin,
                    name = item.name,
                    brand = item.brand,
                    inci_raw = item.inciRaw,
                    rating = item.rating,
                    usage = item.usage.name,
                    category = item.category,
                    saved_at = item.savedAt.ifBlank { Clock.System.now().toString() },
                    inci_hash = storedHash(item)
                )
            }
        }
    }

    override suspend fun remove(shelfKey: String): Outcome<Unit> {
        return FailureCatcher.database("shelf.remove") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.deleteShelf(shelfKey)
            }
        }
    }

    override suspend fun clearAll(): Outcome<Unit> {
        return FailureCatcher.database("shelf.clear") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.clearShelf()
            }
        }
    }

    private fun toItem(row: User_shelf): ShelfItem {
        return ShelfItem(
            shelfKey = row.shelf_key,
            productId = row.product_id,
            gtin = row.gtin,
            name = row.name,
            brand = row.brand,
            inciRaw = row.inci_raw,
            rating = row.rating,
            usage = ProductUsage.parse(row.usage),
            category = row.category,
            savedAt = row.saved_at,
            inciHash = storedHash(row.inci_hash, row.inci_raw)
        )
    }

    private fun storedHash(item: ShelfItem): String {
        return storedHash(item.inciHash, item.inciRaw)
    }

    private fun storedHash(inciHash: String?, inciRaw: String): String {
        if (!inciHash.isNullOrBlank()) {
            return inciHash
        }
        return InciIdentity.hash(inciRaw)
    }
}
