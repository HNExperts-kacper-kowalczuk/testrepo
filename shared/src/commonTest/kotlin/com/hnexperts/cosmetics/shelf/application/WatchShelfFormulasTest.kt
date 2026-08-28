package com.hnexperts.cosmetics.shelf.application

import com.hnexperts.cosmetics.catalog.domain.CachedOnlineProduct
import com.hnexperts.cosmetics.catalog.domain.InciIdentity
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class WatchShelfFormulasTest {
    @Test
    fun staleCatalogHashIsChanged() = runBlocking {
        val product: Product = catalogProduct(inciRaw = "Aqua, Glycerin, Formaldehyde")
        val watch = WatchShelfFormulas(
            products = MemoryProducts(byGtin = mapOf("5901234123457" to product)),
            cache = MemoryCache()
        )
        val item: ShelfItem = shelfItem(
            inciRaw = "Aqua, Glycerin",
            inciHash = InciIdentity.hash("Aqua, Glycerin")
        )

        val changed: Set<String> = requireOk(watch.changedKeys(listOf(item)))

        assertEquals(setOf(item.shelfKey), changed)
    }

    @Test
    fun matchingHashIsUnchanged() = runBlocking {
        val product: Product = catalogProduct(inciRaw = "Aqua, Glycerin")
        val watch = WatchShelfFormulas(
            products = MemoryProducts(byGtin = mapOf("5901234123457" to product)),
            cache = MemoryCache()
        )
        val item: ShelfItem = shelfItem(inciRaw = "aqua,  glycerin", inciHash = "")

        val changed: Set<String> = requireOk(watch.changedKeys(listOf(item)))

        assertTrue(changed.isEmpty())
    }

    @Test
    fun onlineCacheMismatchIsChangedWhenCatalogMisses() = runBlocking {
        val watch = WatchShelfFormulas(
            products = MemoryProducts(),
            cache = MemoryCache(
                items = mapOf(
                    "5901234123457" to CachedOnlineProduct(
                        gtin = "5901234123457",
                        name = "Online cream",
                        brand = null,
                        inciRaw = "Aqua, Glycerin, Formaldehyde",
                        usage = "UNKNOWN",
                        source = "obf",
                        cachedAt = "2026-01-01T00:00:00Z"
                    )
                )
            )
        )
        val item: ShelfItem = shelfItem(
            inciRaw = "Aqua, Glycerin",
            inciHash = InciIdentity.hash("Aqua, Glycerin")
        )

        val changed: Set<String> = requireOk(watch.changedKeys(listOf(item)))

        assertEquals(setOf(item.shelfKey), changed)
    }

    @Test
    fun onlineOnlyWithoutCacheHasNoBadge() = runBlocking {
        val watch = WatchShelfFormulas(MemoryProducts(), MemoryCache())
        val item: ShelfItem = shelfItem(
            inciRaw = "Aqua, Glycerin",
            inciHash = InciIdentity.hash("Aqua, Glycerin")
        )

        val changed: Set<String> = requireOk(watch.changedKeys(listOf(item)))

        assertTrue(changed.isEmpty())
    }

    @Test
    fun productIdLookupDetectsStaleFormula() = runBlocking {
        val product: Product = catalogProduct(id = "gentle-cleanser", inciRaw = "Aqua, Niacinamide")
        val watch = WatchShelfFormulas(
            products = MemoryProducts(byId = mapOf(product.id to product)),
            cache = MemoryCache()
        )
        val item: ShelfItem = shelfItem(
            gtin = null,
            productId = product.id,
            shelfKey = "id:${product.id}",
            inciRaw = "Aqua, Glycerin",
            inciHash = InciIdentity.hash("Aqua, Glycerin")
        )

        val changed: Set<String> = requireOk(watch.changedKeys(listOf(item)))

        assertEquals(setOf(item.shelfKey), changed)
    }

    private fun catalogProduct(
        id: String = "gentle-cleanser",
        inciRaw: String
    ): Product {
        return Product(
            id = id,
            name = "Gentle Cream Cleanser",
            brand = "Fixture Care",
            category = "cleanser",
            inciRaw = inciRaw,
            usage = "RINSE_OFF",
            source = "curated",
            verified = true
        )
    }

    private fun shelfItem(
        gtin: String? = "5901234123457",
        productId: String? = null,
        shelfKey: String = "gtin:5901234123457",
        inciRaw: String,
        inciHash: String
    ): ShelfItem {
        return ShelfItem(
            shelfKey = shelfKey,
            productId = productId,
            gtin = gtin,
            name = "Cleanser",
            brand = null,
            inciRaw = inciRaw,
            rating = "LOW",
            usage = ProductUsage.RINSE_OFF,
            savedAt = "2026-01-01T00:00:00Z",
            inciHash = inciHash
        )
    }

    private fun requireOk(outcome: Outcome<Set<String>>): Set<String> {
        assertTrue(outcome is Outcome.Ok)
        return outcome.value
    }

    private class MemoryProducts(
        private val byGtin: Map<String, Product> = emptyMap(),
        private val byId: Map<String, Product> = emptyMap()
    ) : ProductRepository {
        override suspend fun findByGtin(rawGtin: String): Outcome<Product?> {
            return Outcome.Ok(byGtin[rawGtin])
        }

        override suspend fun findById(productId: String): Outcome<Product?> {
            return Outcome.Ok(byId[productId])
        }

        override suspend fun search(query: String): Outcome<List<Product>> {
            return Outcome.Ok(emptyList())
        }

        override suspend fun findByCategory(category: String, limit: Int): Outcome<List<Product>> {
            return Outcome.Ok(emptyList())
        }
    }

    private class MemoryCache(
        private val items: Map<String, CachedOnlineProduct> = emptyMap()
    ) : OnlineProductCache {
        override suspend fun find(gtin: String): Outcome<CachedOnlineProduct?> {
            return Outcome.Ok(items[gtin])
        }

        override suspend fun put(product: CachedOnlineProduct): Outcome<Unit> {
            return Outcome.Ok(Unit)
        }

        override suspend fun clear(): Outcome<Unit> {
            return Outcome.Ok(Unit)
        }
    }
}
