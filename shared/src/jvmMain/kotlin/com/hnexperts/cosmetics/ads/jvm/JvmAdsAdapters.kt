package com.hnexperts.cosmetics.ads.jvm

import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.ConsentSnapshot
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor

class JvmNetworkMonitor : NetworkMonitor {
    override fun isOnline(): Boolean {
        return false
    }
}

class JvmAdsInitializer : AdsInitializer {
    override suspend fun initialize(): Boolean {
        return false
    }
}

class JvmConsentClient : ConsentClient {
    override suspend fun gather(): ConsentSnapshot {
        return ConsentSnapshot(canRequestAds = false, privacyOptionsRequired = false)
    }

    override suspend fun showPrivacyOptions(): Boolean {
        return false
    }
}
