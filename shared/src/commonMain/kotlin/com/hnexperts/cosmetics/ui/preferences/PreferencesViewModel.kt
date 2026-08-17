package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.CatalogBootstrap
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.preferences.data.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PreferencesViewModel(
    private val repository: SqlPreferencesRepository,
    private val catalog: CatalogBootstrap
) : ViewModel() {
    private val state: MutableStateFlow<StoredPreferences> = MutableStateFlow(
        StoredPreferences(
            profile = UserAvoidanceProfile.EMPTY,
            localePreference = LocalePreference.FOLLOW_SYSTEM,
            pinnedLocale = null
        )
    )
    val preferences: StateFlow<StoredPreferences> = state.asStateFlow()
    private val ingredientState: MutableStateFlow<List<Ingredient>> = MutableStateFlow(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = ingredientState.asStateFlow()
    private val persistMutex: Mutex = Mutex()

    init {
        viewModelScope.launch {
            coroutineScope {
                val storedDeferred = async { repository.load() }
                val indexDeferred = async { catalog.awaitIndex() }
                state.value = storedDeferred.await()
                ingredientState.value = indexDeferred.await().ingredientsSorted
            }
        }
    }

    fun setPregnancyCaution(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(pregnancyCaution = enabled)) }
    }

    fun setFragranceFree(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(fragranceFree = enabled)) }
    }

    fun setFollowSystemLocale() {
        update { current -> current.copy(localePreference = LocalePreference.FOLLOW_SYSTEM, pinnedLocale = null) }
    }

    fun pinLocale(locale: AppLocale) {
        update { current -> current.copy(localePreference = LocalePreference.PINNED, pinnedLocale = locale) }
    }

    fun toggleAvoid(ingredientId: String) {
        update { current ->
            val next: Set<String> = if (current.profile.avoidedIngredientIds.contains(ingredientId)) {
                current.profile.avoidedIngredientIds - ingredientId
            } else {
                current.profile.avoidedIngredientIds + ingredientId
            }
            current.copy(profile = current.profile.copy(avoidedIngredientIds = next))
        }
    }

    private fun update(transform: (StoredPreferences) -> StoredPreferences) {
        viewModelScope.launch {
            persistMutex.withLock {
                val next: StoredPreferences = transform(state.value)
                repository.save(next)
                state.value = next
            }
        }
    }
}
