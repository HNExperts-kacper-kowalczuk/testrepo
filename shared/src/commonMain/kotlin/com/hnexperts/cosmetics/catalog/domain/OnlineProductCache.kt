package com.hnexperts.cosmetics.catalog.domain

import com.hnexperts.cosmetics.failure.Outcome

data class CachedOnlineProduct(
    val gtin: String,
    val name: String,
    val brand: String?,
    val inciRaw: String,
    val usage: String,
    val source: String,
    val cachedAt: String
)

interface OnlineProductCache {
    suspend fun find(gtin: String): Outcome<CachedOnlineProduct?>
    suspend fun put(product: CachedOnlineProduct): Outcome<Unit>
    suspend fun clear(): Outcome<Unit>
}
