package com.hnexperts.cosmetics.catalog.application

object CatalogSyncPaths {
    const val MAX_BYTES: Int = 12 * 1024 * 1024

    fun manifestUrl(baseUrl: String): String {
        return "${trimBase(baseUrl)}/catalog-manifest.json"
    }

    fun deltaUrl(baseUrl: String, fromVersion: String, toVersion: String): String {
        return "${trimBase(baseUrl)}/catalog-delta-$fromVersion-to-$toVersion.json.gz"
    }

    private fun trimBase(baseUrl: String): String {
        return baseUrl.trim().trimEnd('/')
    }
}
