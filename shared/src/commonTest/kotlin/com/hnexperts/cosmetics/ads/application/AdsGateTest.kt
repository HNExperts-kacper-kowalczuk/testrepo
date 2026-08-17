package com.hnexperts.cosmetics.ads.application

import com.hnexperts.cosmetics.ads.AppScreen
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdsGateTest {
    @Test
    fun collapsesWhenSdkIsNotReadyOrBannerFailed() {
        val ready: AdsGate = AdsGate(
            consentGranted = true,
            networkAvailable = true,
            sdkReady = true,
            bannerLoadFailed = false
        )
        assertTrue(ready.bannerVisible(AppScreen.RESULT))
        assertFalse(ready.bannerVisible(AppScreen.SCAN))
        assertFalse(ready.bannerVisible(AppScreen.CAMERA))
        assertFalse(ready.bannerVisible(AppScreen.OCR_REVIEW))
        assertFalse(ready.copy(sdkReady = false).bannerVisible(AppScreen.RESULT))
        assertFalse(ready.copy(bannerLoadFailed = true).bannerVisible(AppScreen.RESULT))
        assertFalse(ready.copy(networkAvailable = false).bannerVisible(AppScreen.RESULT))
        assertFalse(ready.copy(consentGranted = false).bannerVisible(AppScreen.RESULT))
    }
}
