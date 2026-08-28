package com.hnexperts.cosmetics.ads

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdMobConfigTest {
    @Test
    fun jvmBuildIsNotConfiguredSoBannersStayCollapsed() {
        assertTrue(AdMobConfig.bannerUnitId.isEmpty())
        assertFalse(AdMobConfig.isConfigured)
    }
}
