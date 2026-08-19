package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.ads.application.AdsGate
import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.ads.domain.BillingPort
import com.hnexperts.cosmetics.catalog.application.ApplyCatalogDelta
import com.hnexperts.cosmetics.catalog.application.CatalogFreshness
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdates
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.platform.copyPlainText
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
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
    val catalogApplied: Boolean = false,
    val failure: AppFailure? = null,
    val avoidQuery: String = "",
    val openReportCount: Long = 0,
    val reportsCopied: Boolean = false,
    val adsRemoved: Boolean = false
)

class PreferencesViewModel(
    private val repository: PreferencesStore,
    private val catalog: CatalogGateway,
    private val catalogUpdates: CheckCatalogUpdates,
    private val applyCatalogDelta: ApplyCatalogDelta,
    private val adsSession: AdsSession,
    private val history: ScanHistoryRepository,
    private val reports: ReportQueue,
    private val billing: BillingPort
) : ViewModel() {
    private val state: MutableStateFlow<PreferencesUiState> = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = state.asStateFlow()
    private val persistMutex: Mutex = Mutex()
    private var catalogIngredients: List<Ingredient> = emptyList()

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
                val reportsDeferred = async { reports.openCount() }
                val stored: Outcome<StoredPreferences> = storedDeferred.await()
                val index = indexDeferred.await()
                val freshness = freshnessDeferred.await()
                val reportCount: Long = reportsDeferred.await().getOrNull() ?: 0L
                val ingredientsOutcome: Outcome<List<Ingredient>> = when (index) {
                    is Outcome.Ok -> Outcome.Ok(index.value.ingredientsSorted)
                    is Outcome.Err -> index
                }
                val combined: Outcome<Pair<StoredPreferences, List<Ingredient>>> =
                    Outcome.zip(stored, ingredientsOutcome)
                when (combined) {
                    is Outcome.Ok -> {
                        catalogIngredients = combined.value.second
                        val storedPrefs: StoredPreferences = combined.value.first
                        state.value = state.value.copy(
                            stored = storedPrefs,
                            ingredients = displayedAvoid(catalogIngredients, storedPrefs.profile, state.value.avoidQuery),
                            catalogMeta = (index as? Outcome.Ok)?.value?.meta,
                            freshness = freshness.getOrNull(),
                            failure = (freshness as? Outcome.Err)?.failure,
                            openReportCount = reportCount,
                            adsRemoved = storedPrefs.adsRemoved
                        )
                    }
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

    fun setEuAllergens(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(euAllergens = enabled)) }
    }

    fun setChildrenCaution(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(childrenCaution = enabled)) }
    }

    fun setAlcoholLeaveOn(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(alcoholLeaveOn = enabled)) }
    }

    fun setEssentialOilCluster(enabled: Boolean) {
        update { current -> current.copy(profile = current.profile.copy(essentialOilCluster = enabled)) }
    }

    fun setAvoidQuery(text: String) {
        state.value = state.value.copy(
            avoidQuery = text,
            ingredients = displayedAvoid(catalogIngredients, state.value.stored.profile, text)
        )
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

    fun applyCatalogUpdate() {
        val freshness: CatalogFreshness.UpdateAvailable =
            state.value.freshness as? CatalogFreshness.UpdateAvailable ?: return
        viewModelScope.launch {
            runUiAction(::showFailure) { applyCatalogDelta.invoke(freshness.published) } ?: return@launch
            state.value = state.value.copy(catalogApplied = true, failure = null)
            reload()
        }
    }

    fun copyReports() {
        viewModelScope.launch {
            val items = runUiAction(::showFailure) { reports.openReports() } ?: return@launch
            val text: String = items.joinToString(separator = "\n") { report ->
                "${report.kind}\t${report.gtin.orEmpty()}\t${report.payloadJson}"
            }
            copyPlainText(text.ifBlank { "kind\tgtin\tpayload" })
            state.value = state.value.copy(reportsCopied = true, failure = null)
        }
    }

    fun purchaseRemoveAds() {
        viewModelScope.launch {
            val purchased = runUiAction(::showFailure) { billing.purchaseRemoveAds() } ?: return@launch
            if (!purchased) {
                return@launch
            }
            persistMutex.withLock {
                val next: StoredPreferences = state.value.stored.copy(adsRemoved = true)
                val saved = runUiAction(::showFailure) { repository.save(next) }
                if (saved != null) {
                    state.value = state.value.copy(stored = next, adsRemoved = true, failure = null)
                    adsSession.refresh()
                }
            }
        }
    }

    private fun update(transform: (StoredPreferences) -> StoredPreferences) {
        viewModelScope.launch {
            persistMutex.withLock {
                val next: StoredPreferences = transform(state.value.stored)
                val saved = runUiAction(onFailure = ::showFailure) { repository.save(next) }
                if (saved != null) {
                    state.value = state.value.copy(
                        stored = next,
                        ingredients = displayedAvoid(catalogIngredients, next.profile, state.value.avoidQuery),
                        failure = null
                    )
                }
            }
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.value = state.value.copy(failure = failure)
    }

    private fun displayedAvoid(
        catalog: List<Ingredient>,
        profile: UserAvoidanceProfile,
        query: String
    ): List<Ingredient> {
        val needle: String = query.trim()
        if (needle.isEmpty()) {
            return catalog.filter { ingredient -> profile.avoidedIngredientIds.contains(ingredient.id) }
        }
        val lowered: String = needle.lowercase()
        return catalog
            .filter { ingredient -> ingredient.inciName.lowercase().contains(lowered) }
            .take(AVOID_SEARCH_LIMIT)
    }

    private companion object {
        const val AVOID_SEARCH_LIMIT: Int = 40
    }
}
