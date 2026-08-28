package com.hnexperts.cosmetics.preferences.domain

import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference

data class StoredPreferences(
    val profile: UserAvoidanceProfile,
    val localePreference: LocalePreference,
    val pinnedLocale: AppLocale?,
    val adsRemoved: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.FOLLOW_SYSTEM
)

interface PreferencesStore {
    suspend fun load(): Outcome<StoredPreferences>
    suspend fun save(preferences: StoredPreferences): Outcome<Unit>
}
