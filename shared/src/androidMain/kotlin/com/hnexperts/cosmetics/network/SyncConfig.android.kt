package com.hnexperts.cosmetics.network

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.hnexperts.cosmetics.di.AndroidAppContext

actual object SyncConfig {
    actual val catalogBaseUrl: String
        get() = meta(CATALOG_META)

    actual val reportsUrl: String
        get() = meta(REPORTS_META)

    actual val isCatalogConfigured: Boolean
        get() = catalogBaseUrl.isNotBlank()

    actual val isReportsConfigured: Boolean
        get() = reportsUrl.isNotBlank()

    private fun meta(key: String): String {
        val context = AndroidAppContext.current() ?: return ""
        val flags: Int = PackageManager.GET_META_DATA
        val info: ApplicationInfo = context.packageManager.getApplicationInfo(context.packageName, flags)
        return info.metaData?.getString(key)?.trim().orEmpty()
    }

    private const val CATALOG_META: String = "com.hnexperts.cosmetics.sync.CATALOG_BASE_URL"
    private const val REPORTS_META: String = "com.hnexperts.cosmetics.sync.REPORTS_URL"
}
