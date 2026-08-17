package com.hnexperts.cosmetics.ads.application

import com.hnexperts.cosmetics.ads.AdPolicy
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.ConsentSnapshot
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AdsGate(
    val consentGranted: Boolean = false,
    val networkAvailable: Boolean = false,
    val sdkReady: Boolean = false,
    val bannerLoadFailed: Boolean = false,
    val privacyOptionsRequired: Boolean = false
) {
    fun bannerVisible(screen: AppScreen, policy: AdPolicy = AdPolicy()): Boolean {
        if (!sdkReady || bannerLoadFailed) {
            return false
        }
        return policy.shouldShowBanner(screen, consentGranted, networkAvailable)
    }
}

class AdsSession(
    private val consent: ConsentClient,
    private val network: NetworkMonitor,
    private val adsInitializer: AdsInitializer,
    applicationScope: ApplicationScope
) {
    private val state: MutableStateFlow<AdsGate> = MutableStateFlow(AdsGate())
    val gate: StateFlow<AdsGate> = state.asStateFlow()
    private val mutex: Mutex = Mutex()

    init {
        applicationScope.coroutineScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        mutex.withLock {
            val online: Boolean = network.isOnline()
            val snapshot: ConsentSnapshot = try {
                consent.gather()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AppLog.e("ads.consent", error.message ?: error::class.simpleName.orEmpty(), error)
                ConsentSnapshot(canRequestAds = false, privacyOptionsRequired = false)
            }
            var sdkReady: Boolean = false
            if (snapshot.canRequestAds && online) {
                sdkReady = try {
                    adsInitializer.initialize()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    AppLog.e("ads.sdk", error.message ?: error::class.simpleName.orEmpty(), error)
                    false
                }
            }
            state.update { current ->
                current.copy(
                    consentGranted = snapshot.canRequestAds,
                    networkAvailable = online,
                    sdkReady = sdkReady,
                    privacyOptionsRequired = snapshot.privacyOptionsRequired,
                    bannerLoadFailed = if (sdkReady) current.bannerLoadFailed else false
                )
            }
        }
    }

    fun markBannerFailed() {
        state.update { current -> current.copy(bannerLoadFailed = true) }
    }

    suspend fun openPrivacyOptions(): Boolean {
        return try {
            consent.showPrivacyOptions()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AppLog.e("ads.privacy", error.message ?: error::class.simpleName.orEmpty(), error)
            false
        }
    }
}
