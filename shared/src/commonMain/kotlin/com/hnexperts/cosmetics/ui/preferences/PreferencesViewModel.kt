package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.ads.application.AdsGate
import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.catalog.application.CatalogFreshness
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdates
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
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
    val catalogMeta: CatalogMeta? = null,
    val freshness: CatalogFreshness? = null,
    val ads: AdsGate = AdsGate(),
    val historyCleared: Boolean = false,
    val failure: AppFailure? = null
)

class PreferencesViewModel(
    private val repository: PreferencesStore,
    private val catalog: CatalogGateway,
    private val catalogUpdates: CheckCatalogUpdates,
    private val adsSession: AdsSession,
    private val history: ScanHistoryRepository
) : ViewModel() {
    private val state: MutableStateFlow<PreferencesUiState> = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = state.asStateFlow()
    private val persistMutex: Mutex = Mutex()

    init {
        reload()
        viewModelScope.launch {
            adsSession.gate.collect { gate ->
                state.value = state.value.copy(ads = gate)
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            coroutineScope {
                val storedDeferred = async { repository.load() }
                val indexDeferred = async { catalog.awaitIndex() }
                val freshnessDeferred = async { catalogUpdates.invoke() }
                val stored: Outcome<StoredPreferences> = storedDeferred.await()
                val index = indexDeferred.await()
                val freshness = freshnessDeferred.await()
                val ingredientsOutcome: Outcome<List<Ingredient>> = when (index) {
                    is Outcome.Ok -> Outcome.Ok(index.value.ingredientsSorted)
                    is Outcome.Err -> index
                }
                val combined: Outcome<Pair<StoredPreferences, List<Ingredient>>> =
                    Outcome.zip(stored, ingredientsOutcome)
                when (combined) {
                    is Outcome.Ok -> state.value = state.value.copy(
                        stored = combined.value.first,
                        ingredients = combined.value.second,
                        catalogMeta = (index as? Outcome.Ok)?.value?.meta,
                        freshness = freshness.getOrNull(),
                        failure = (freshness as? Outcome.Err)?.failure
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

    fun openPrivacyOptions() {
        viewModelScope.launch {
            adsSession.openPrivacyOptions()
            adsSession.refresh()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            runUiAction(::showFailure) { history.clear() } ?: return@launch
            state.value = state.value.copy(historyCleared = true, failure = null)
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
