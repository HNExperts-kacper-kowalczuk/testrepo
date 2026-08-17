package com.hnexperts.cosmetics.catalog.application

import kotlinx.serialization.Serializable

@Serializable
data class CatalogManifest(
    val catalogVersion: String,
    val rulesetVersion: String,
    val builtAt: String,
    val region: String,
    val checksum: String,
    val productCount: Int,
    val ingredientCount: Int
)

sealed class CatalogFreshness {
    data class UpToDate(val manifest: CatalogManifest) : CatalogFreshness()
    data class UpdateAvailable(val local: CatalogManifest, val published: CatalogManifest) : CatalogFreshness()
    data object Offline : CatalogFreshness()
}

interface CatalogRemote {
    suspend fun publishedManifest(): CatalogManifest
}
