package com.hnexperts.cosmetics.preferences.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlinx.coroutines.withContext

class SqlPreferencesRepository(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : PreferencesStore {
    override suspend fun load(): Outcome<StoredPreferences> {
        return FailureCatcher.database("preferences.load") {
            withContext(dispatchers.userDatabase) {
                ensureProfile()
                val row = database.userDatabaseQueries.selectProfile().executeAsOne()
                val avoidIds: Set<String> = database.userDatabaseQueries.selectAvoidIds().executeAsList().toSet()
                val localePreference: LocalePreference =
                    if (row.locale_preference == "pinned") LocalePreference.PINNED else LocalePreference.FOLLOW_SYSTEM
                val pinned: AppLocale? = row.pinned_locale?.let { tag -> AppLocale.parse(tag) }
                StoredPreferences(
                    profile = UserAvoidanceProfile(
                        pregnancyCaution = row.pregnancy_caution != 0L,
                        fragranceFree = row.fragrance_free != 0L,
                        avoidedIngredientIds = avoidIds
                    ),
                    localePreference = localePreference,
                    pinnedLocale = pinned
                )
            }
        }
    }

    override suspend fun save(preferences: StoredPreferences): Outcome<Unit> {
        return FailureCatcher.database("preferences.save") {
            withContext(dispatchers.userDatabase) {
                database.transaction {
                    database.userDatabaseQueries.upsertProfile(
                        pregnancy_caution = if (preferences.profile.pregnancyCaution) 1 else 0,
                        fragrance_free = if (preferences.profile.fragranceFree) 1 else 0,
                        locale_preference = if (preferences.localePreference == LocalePreference.PINNED) "pinned" else "system",
                        pinned_locale = preferences.pinnedLocale?.tag
                    )
                    database.userDatabaseQueries.clearAvoid()
                    for (id in preferences.profile.avoidedIngredientIds) {
                        database.userDatabaseQueries.insertAvoid(id)
                    }
                }
            }
        }
    }

    private fun ensureProfile() {
        val existing = database.userDatabaseQueries.selectProfile().executeAsOneOrNull()
        if (existing == null) {
            database.userDatabaseQueries.upsertProfile(0, 0, "system", null)
        }
    }
}
