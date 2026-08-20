package com.hnexperts.cosmetics.preferences.application

import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.shelf.domain.UserShelf

/**
 * Wipes data this app stored in the user database.
 * Never touches the offline catalog or the legal disclaimer flag.
 */
class UserDataReset(
    private val history: ScanHistoryRepository,
    private val shelf: UserShelf,
    private val reports: ReportQueue,
    private val cache: OnlineProductCache,
    private val preferences: PreferencesStore
) {
    suspend fun clearHistory(): Outcome<Unit> {
        return history.clear()
    }

    suspend fun clearShelf(): Outcome<Unit> {
        return shelf.clearAll()
    }

    suspend fun clearAvoidList(): Outcome<Unit> {
        val stored: StoredPreferences = when (val loaded: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Err -> return loaded
            is Outcome.Ok -> loaded.value
        }
        val cleared: StoredPreferences = stored.copy(
            profile = stored.profile.copy(avoidedIngredientIds = emptySet())
        )
        return preferences.save(cleared)
    }

    suspend fun resetDevice(): Outcome<Unit> {
        val steps: List<suspend () -> Outcome<Unit>> = listOf(
            history::clear,
            shelf::clearAll,
            reports::clear,
            cache::clear,
            ::resetPersonalProfile
        )
        return runAll(steps)
    }

    private suspend fun resetPersonalProfile(): Outcome<Unit> {
        val stored: StoredPreferences = when (val loaded: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Err -> return loaded
            is Outcome.Ok -> loaded.value
        }
        val reset: StoredPreferences = stored.copy(
            profile = UserAvoidanceProfile.EMPTY,
            adsRemoved = false
        )
        return preferences.save(reset)
    }

    private suspend fun runAll(steps: List<suspend () -> Outcome<Unit>>): Outcome<Unit> {
        for (step in steps) {
            when (val result: Outcome<Unit> = step()) {
                is Outcome.Err -> return result
                is Outcome.Ok -> Unit
            }
        }
        return Outcome.Ok(Unit)
    }
}
