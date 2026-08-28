package com.hnexperts.cosmetics.preferences.application

import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Live appearance for [com.hnexperts.cosmetics.ui.theme.CosmeticsTheme].
 * Loads from [PreferencesStore] off the UI thread; [publish] is called after a save.
 */
class ThemeSession(
    private val preferences: PreferencesStore,
    applicationScope: ApplicationScope
) {
    private val state: MutableStateFlow<ThemePreference> =
        MutableStateFlow(ThemePreference.FOLLOW_SYSTEM)
    val preference: StateFlow<ThemePreference> = state.asStateFlow()

    init {
        applicationScope.coroutineScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        when (val loaded: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Ok -> state.value = loaded.value.themePreference
            is Outcome.Err -> Unit
        }
    }

    fun publish(preference: ThemePreference) {
        state.value = preference
    }
}
