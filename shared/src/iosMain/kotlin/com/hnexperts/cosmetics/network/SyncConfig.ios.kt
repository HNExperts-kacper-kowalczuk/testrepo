package com.hnexperts.cosmetics.network

import platform.Foundation.NSBundle

actual object SyncConfig {
    actual val catalogBaseUrl: String
        get() = plist("CatalogSyncURL")

    actual val reportsUrl: String
        get() = plist("ReportsFlushURL")

    actual val isCatalogConfigured: Boolean
        get() = catalogBaseUrl.isNotBlank()

    actual val isReportsConfigured: Boolean
        get() = reportsUrl.isNotBlank()

    private fun plist(key: String): String {
        return (NSBundle.mainBundle.objectForInfoDictionaryKey(key) as? String)
            ?.trim()
            .orEmpty()
    }
}
