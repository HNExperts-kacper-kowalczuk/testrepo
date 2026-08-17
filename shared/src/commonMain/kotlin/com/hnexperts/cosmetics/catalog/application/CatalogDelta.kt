package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct

data class CatalogDelta(
    val fromCatalogVersion: String,
    val meta: CatalogMeta,
    val ingredients: List<FixtureIngredient>,
    val products: List<FixtureProduct>
)

interface CatalogMutationStore {
    fun hasCurrentCatalog(version: String, checksum: String): Boolean
    fun isEmpty(): Boolean
    fun replaceAll(
        ingredients: List<FixtureIngredient>,
        products: List<FixtureProduct>,
        meta: CatalogMeta
    )
    fun applyDelta(delta: CatalogDelta)
    fun upsertMeta(meta: CatalogMeta)
    fun clearAll()
}

interface CatalogDeltaSource {
    suspend fun deltaFor(fromVersion: String, toVersion: String): CatalogDelta?
}

class BundledCatalogDeltaSource : CatalogDeltaSource {
    override suspend fun deltaFor(fromVersion: String, toVersion: String): CatalogDelta? {
        return null
    }
}
