package com.hnexperts.cosmetics.preferences.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.data.userdb.User_profile
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
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
                mapStored(database.userDatabaseQueries.selectProfile().executeAsOne())
            }
        }
    }

    override suspend fun save(preferences: StoredPreferences): Outcome<Unit> {
        return FailureCatcher.database("preferences.save") {
            withContext(dispatchers.userDatabase) {
                database.transaction {
                    writeProfile(preferences)
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
            database.userDatabaseQueries.upsertProfile(
                0, 0, "system", null, 0, 0, 0, 0, 0, ThemePreference.FOLLOW_SYSTEM.storageValue()
            )
        }
    }

    private fun mapStored(row: User_profile): StoredPreferences {
        val avoidIds: Set<String> = database.userDatabaseQueries.selectAvoidIds().executeAsList().toSet()
        val localePreference: LocalePreference =
            if (row.locale_preference == "pinned") LocalePreference.PINNED else LocalePreference.FOLLOW_SYSTEM
        val pinned: AppLocale? = row.pinned_locale?.let { tag -> AppLocale.parse(tag) }
        return StoredPreferences(
            profile = UserAvoidanceProfile(
                pregnancyCaution = row.pregnancy_caution != 0L,
                fragranceFree = row.fragrance_free != 0L,
                avoidedIngredientIds = avoidIds,
                euAllergens = row.eu_allergens != 0L,
                childrenCaution = row.children_caution != 0L,
                alcoholLeaveOn = row.alcohol_leave_on != 0L,
                essentialOilCluster = row.essential_oil_cluster != 0L
            ),
            localePreference = localePreference,
            pinnedLocale = pinned,
            adsRemoved = row.ads_removed != 0L,
            themePreference = ThemePreference.fromStorage(row.theme_preference)
        )
    }

    private fun writeProfile(preferences: StoredPreferences) {
        val profile: UserAvoidanceProfile = preferences.profile
        database.userDatabaseQueries.upsertProfile(
            pregnancy_caution = flag(profile.pregnancyCaution),
            fragrance_free = flag(profile.fragranceFree),
            locale_preference = if (preferences.localePreference == LocalePreference.PINNED) "pinned" else "system",
            pinned_locale = preferences.pinnedLocale?.tag,
            eu_allergens = flag(profile.euAllergens),
            children_caution = flag(profile.childrenCaution),
            alcohol_leave_on = flag(profile.alcoholLeaveOn),
            essential_oil_cluster = flag(profile.essentialOilCluster),
            ads_removed = flag(preferences.adsRemoved),
            theme_preference = preferences.themePreference.storageValue()
        )
    }

    private fun flag(enabled: Boolean): Long {
        return if (enabled) 1 else 0
    }
}
