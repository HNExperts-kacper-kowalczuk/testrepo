package com.hnexperts.cosmetics.i18n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun systemAppLocale(): AppLocale {
    val preferred: String = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
    return AppLocale.parse(preferred)
}
