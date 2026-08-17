package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class PreferencesUiState(
    val stored: StoredPreferences = StoredPreferences(
        profile = UserAvoidanceProfile.EMPTY,
        localePreference = LocalePreference.FOLLOW_SYSTEM,
        pinnedLocale = null
    ),
    val ingredients: List<Ingredient> = emptyList(),
    val failure: AppFailure? = null
)

class PreferencesViewModel(
    private val repository: PreferencesStore,
    private val catalog: CatalogGateway
) : ViewModel() {
    private val state: MutableStateFlow<PreferencesUiState> = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = state.asStateFlow()
    private val persistMutex: Mutex = Mutex()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            coroutineScope {
                val storedDeferred = async { repository.load() }
                val indexDeferred = async { catalog.awaitIndex() }
                val combined: Outcome<Pair<StoredPreferences, List<Ingredient>>> = Outcome.zip(
                    storedDeferred.await(),
                    when (val index = indexDeferred.await()) {
                        is Outcome.Ok -> Outcome.Ok(index.value.ingredientsSorted)
                        is Outcome.Err -> index
                    }
                )
                when (combined) {
                    is Outcome.Ok -> state.value = PreferencesUiState(
                        stored = combined.value.first,
                        ingredients = combined.value.second,
                        failure = null
                    )
                    is Outcome.Err -> state.value = state.value.copy(failure = combined.failure)
                }
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
                val next: StoredPreferences = transform(state.value.stored)
                val saved = runUiAction(onFailure = ::showFailure) { repository.save(next) }
                if (saved != null) {
                    state.value = state.value.copy(stored = next, failure = null)
                }
            }
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.value = state.value.copy(failure = failure)
    }
}
