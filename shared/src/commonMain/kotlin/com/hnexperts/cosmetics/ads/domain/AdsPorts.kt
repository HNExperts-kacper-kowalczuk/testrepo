package com.hnexperts.cosmetics.ads.domain

data class ConsentSnapshot(
    val canRequestAds: Boolean,
    val privacyOptionsRequired: Boolean
)

interface ConsentClient {
    suspend fun gather(): ConsentSnapshot
    suspend fun showPrivacyOptions(): Boolean
}

interface NetworkMonitor {
    fun isOnline(): Boolean
}

interface AdsInitializer {
    suspend fun initialize(): Boolean
}
