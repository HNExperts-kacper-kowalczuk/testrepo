package com.hnexperts.cosmetics.ads

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.hnexperts.cosmetics.di.AndroidAppContext

actual object AdMobConfig {
    actual val bannerUnitId: String
        get() = meta(BANNER_META)

    actual val isConfigured: Boolean
        get() = bannerUnitId.isNotBlank()

    private fun meta(key: String): String {
        val context = AndroidAppContext.current() ?: return ""
        val flags: Int = PackageManager.GET_META_DATA
        val info: ApplicationInfo = context.packageManager.getApplicationInfo(context.packageName, flags)
        return info.metaData?.getString(key)?.trim().orEmpty()
    }

    private const val BANNER_META: String = "com.hnexperts.cosmetics.ads.BANNER_UNIT_ID"
}
