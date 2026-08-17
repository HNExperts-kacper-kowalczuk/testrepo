package com.hnexperts.cosmetics.ads.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.ConsentSnapshot
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.di.AndroidAppContext
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AndroidNetworkMonitor : NetworkMonitor {
    override fun isOnline(): Boolean {
        val context = AndroidAppContext.current() ?: return false
        val manager: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities: NetworkCapabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class AndroidAdsInitializer : AdsInitializer {
    private val mutex: Mutex = Mutex()
    @Volatile
    private var ready: Boolean = false

    override suspend fun initialize(): Boolean {
        mutex.withLock {
            if (ready) {
                return true
            }
            val context = AndroidAppContext.current() ?: return false
            return suspendCancellableCoroutine { continuation ->
                MobileAds.initialize(context) {
                    ready = true
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
            }
        }
    }
}

class AndroidConsentClient : ConsentClient {
    override suspend fun gather(): ConsentSnapshot {
        val activity = AndroidAppContext.activity()
        val context = AndroidAppContext.current() ?: return ConsentSnapshot(false, false)
        val info: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (activity != null) {
            requestUpdate(activity, info)
            loadFormIfRequired(activity)
        }
        return ConsentSnapshot(
            canRequestAds = info.canRequestAds(),
            privacyOptionsRequired = info.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        )
    }

    override suspend fun showPrivacyOptions(): Boolean {
        val activity = AndroidAppContext.activity() ?: return false
        return suspendCancellableCoroutine { continuation ->
            UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                if (continuation.isActive) {
                    continuation.resume(true)
                }
            }
        }
    }

    private suspend fun requestUpdate(activity: android.app.Activity, info: ConsentInformation) {
        val params: ConsentRequestParameters = ConsentRequestParameters.Builder().build()
        suspendCancellableCoroutine { continuation ->
            info.requestConsentInfoUpdate(
                activity,
                params,
                {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
                {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            )
        }
    }

    private suspend fun loadFormIfRequired(activity: android.app.Activity) {
        suspendCancellableCoroutine { continuation ->
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }
}
