package com.hnexperts.cosmetics.ads

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdPolicyTest {
    private val policy: AdPolicy = AdPolicy()

    @Test
    fun neverShowsOnScanOrOcrReview() {
        assertFalse(policy.shouldShowBanner(AppScreen.SCAN, consentGranted = true, networkAvailable = true))
        assertFalse(policy.shouldShowBanner(AppScreen.CAMERA, consentGranted = true, networkAvailable = true))
        assertFalse(policy.shouldShowBanner(AppScreen.OCR_REVIEW, consentGranted = true, networkAvailable = true))
        assertFalse(policy.shouldShowBanner(AppScreen.PREFERENCES, consentGranted = true, networkAvailable = true))
    }

    @Test
    fun showsOnResultWhenConsentAndNetworkExist() {
        assertTrue(policy.shouldShowBanner(AppScreen.RESULT, consentGranted = true, networkAvailable = true))
        assertFalse(policy.shouldShowBanner(AppScreen.RESULT, consentGranted = false, networkAvailable = true))
        assertFalse(policy.shouldShowBanner(AppScreen.RESULT, consentGranted = true, networkAvailable = false))
    }
}
