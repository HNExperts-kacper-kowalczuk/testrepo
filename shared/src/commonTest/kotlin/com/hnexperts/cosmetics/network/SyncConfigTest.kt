package com.hnexperts.cosmetics.network

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncConfigTest {
    @Test
    fun jvmBuildLeavesHostedCatalogAndReportsUnconfigured() {
        assertTrue(SyncConfig.catalogBaseUrl.isEmpty())
        assertTrue(SyncConfig.reportsUrl.isEmpty())
        assertFalse(SyncConfig.isCatalogConfigured)
        assertFalse(SyncConfig.isReportsConfigured)
    }
}
