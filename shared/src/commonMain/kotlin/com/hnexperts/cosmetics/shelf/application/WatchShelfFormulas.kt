package com.hnexperts.cosmetics.shelf.application

import com.hnexperts.cosmetics.catalog.domain.InciIdentity
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.shelf.domain.ShelfItem

class WatchShelfFormulas(
    private val products: ProductRepository,
    private val cache: OnlineProductCache
) {
    suspend fun changedKeys(items: List<ShelfItem>): Outcome<Set<String>> {
        val changed: MutableSet<String> = mutableSetOf()
        for (item in items) {
            when (val current: Outcome<String?> = currentInci(item)) {
                is Outcome.Err -> return current
                is Outcome.Ok -> {
                    val catalogInci: String = current.value ?: continue
                    if (InciIdentity.hash(catalogInci) != storedHash(item)) {
                        changed += item.shelfKey
                    }
                }
            }
        }
        return Outcome.Ok(changed)
    }

    private suspend fun currentInci(item: ShelfItem): Outcome<String?> {
        val byId: Outcome<Product?> = lookupById(item.productId)
        when (byId) {
            is Outcome.Err -> return byId
            is Outcome.Ok -> {
                val product: Product? = byId.value
                if (product != null) {
                    return Outcome.Ok(product.inciRaw)
                }
            }
        }
        return lookupByGtin(item.gtin)
    }

    private suspend fun lookupById(productId: String?): Outcome<Product?> {
        if (productId.isNullOrBlank()) {
            return Outcome.Ok(null)
        }
        return products.findById(productId)
    }

    private suspend fun lookupByGtin(gtin: String?): Outcome<String?> {
        if (gtin.isNullOrBlank()) {
            return Outcome.Ok(null)
        }
        val catalog: Outcome<Product?> = products.findByGtin(gtin)
        when (catalog) {
            is Outcome.Err -> return catalog
            is Outcome.Ok -> {
                val product: Product? = catalog.value
                if (product != null) {
                    return Outcome.Ok(product.inciRaw)
                }
            }
        }
        return when (val cached = cache.find(gtin)) {
            is Outcome.Err -> cached
            is Outcome.Ok -> Outcome.Ok(cached.value?.inciRaw)
        }
    }

    private fun storedHash(item: ShelfItem): String {
        if (item.inciHash.isNotBlank()) {
            return item.inciHash
        }
        return InciIdentity.hash(item.inciRaw)
    }
}
