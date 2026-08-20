package com.hnexperts.cosmetics.catalog.domain

import com.hnexperts.cosmetics.failure.Outcome

interface ProductRepository {
    suspend fun findByGtin(rawGtin: String): Outcome<Product?>
    suspend fun search(query: String): Outcome<List<Product>>
    suspend fun findByCategory(category: String, limit: Int): Outcome<List<Product>>
}
