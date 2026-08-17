package com.hnexperts.cosmetics.ads.ios

import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.ConsentSnapshot
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined

class IosNetworkMonitor : NetworkMonitor {
    override fun isOnline(): Boolean {
        return true
    }
}

class IosAdsInitializer : AdsInitializer {
    override suspend fun initialize(): Boolean {
        return false
    }
}

class IosConsentClient : ConsentClient {
    override suspend fun gather(): ConsentSnapshot {
        if (ATTrackingManager.trackingAuthorizationStatus == ATTrackingManagerAuthorizationStatusNotDetermined) {
            requestTracking()
        }
        val resolved: Boolean =
            ATTrackingManager.trackingAuthorizationStatus != ATTrackingManagerAuthorizationStatusNotDetermined
        return ConsentSnapshot(
            canRequestAds = resolved,
            privacyOptionsRequired = false
        )
    }

    override suspend fun showPrivacyOptions(): Boolean {
        return false
    }

    private suspend fun requestTracking() {
        suspendCancellableCoroutine { continuation ->
            ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { _ ->
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }
}
