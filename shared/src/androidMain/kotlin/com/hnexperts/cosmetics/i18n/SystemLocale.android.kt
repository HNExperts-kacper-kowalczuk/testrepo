package com.hnexperts.cosmetics.i18n

import java.util.Locale

actual fun systemAppLocale(): AppLocale {
    val language: String = Locale.getDefault().language.ifBlank { "en" }
    val country: String = Locale.getDefault().country
    val region: String? = country.ifBlank { null }
    return AppLocale(language = language, region = region)
}
