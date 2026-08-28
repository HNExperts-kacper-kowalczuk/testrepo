package com.hnexperts.cosmetics.network

expect object SyncConfig {
    val catalogBaseUrl: String
    val reportsUrl: String
    val isCatalogConfigured: Boolean
    val isReportsConfigured: Boolean
}
