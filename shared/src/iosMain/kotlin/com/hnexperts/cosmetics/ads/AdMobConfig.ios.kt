package com.hnexperts.cosmetics.ads

import platform.Foundation.NSBundle

actual object AdMobConfig {
    actual val bannerUnitId: String
        get() = (NSBundle.mainBundle.objectForInfoDictionaryKey("GADBannerUnitID") as? String)
            ?.trim()
            .orEmpty()

    actual val isConfigured: Boolean
        get() = bannerUnitId.isNotBlank()
}
