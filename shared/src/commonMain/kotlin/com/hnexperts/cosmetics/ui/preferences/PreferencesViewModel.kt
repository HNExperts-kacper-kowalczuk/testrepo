package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.ads.application.AdsGate
import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.ads.domain.BillingPort
import com.hnexperts.cosmetics.catalog.application.ApplyCatalogDelta
import com.hnexperts.cosmetics.catalog.application.CatalogFreshness
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdates
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.platform.copyPlainText
import com.hnexperts.cosmetics.preferences.application.PreferencesExportText
import com.hnexperts.cosmetics.preferences.application.ThemeSession
import com.hnexperts.cosmetics.preferences.application.UserDataReset
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.application.FlushReports
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.shelf.domain.UserShelf
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class DataResetKind {
    HISTORY,
    SHELF,
    AVOID_LIST,
    DEVICE
}

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
    val catalogApplied: Boolean = false,
    val failure: AppFailure? = null,
    val avoidQuery: String = "",
    val openReportCount: Long = 0,
    val reportsCopied: Boolean = false,
    val reportsSent: Boolean = false,
    val reportsSendAvailable: Boolean = false,
    val adsRemoved: Boolean = false,
    val billingAvailable: Boolean = false,
    val pendingReset: DataResetKind? = null,
    val cleared: DataResetKind? = null,
    val avoidCopied: Boolean = false,
    val shelfCopied: Boolean = false
)

class PreferencesViewModel(
    private val repository: PreferencesStore,
    private val catalog: CatalogGateway,
    private val catalogUpdates: CheckCatalogUpdates,
    private val applyCatalogDelta: ApplyCatalogDelta,
    private val adsSession: AdsSession,
    private val reports: ReportQueue,
    private val flushReports: FlushReports,
    private val billing: BillingPort,
    private val userDataReset: UserDataReset,
    private val shelf: UserShelf,
    private val themeSession: ThemeSession
) : ViewModel() {
    private val state: MutableStateFlow<PreferencesUiState> = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = state.asStateFlow()
    private val persistMutex: Mutex = Mutex()
    private var catalogIndex: CatalogIndex? = null
    private var ingredientsById: Map<String, Ingredient> = emptyMap()

    init {
        state.value = state.value.copy(
            billingAvailable = billing.isAvailable(),
            reportsSendAvailable = flushReports.isConfigured()
        )
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
                when (stored) {
                    is Outcome.Ok -> {
                        val indexValue: CatalogIndex? = (index as? Outcome.Ok)?.value
                        catalogIndex = indexValue
                        ingredientsById = indexValue?.ingredientsById.orEmpty()
                        val storedPrefs: StoredPreferences = stored.value
                        themeSession.publish(storedPrefs.themePreference)
                        state.value = state.value.copy(
                            stored = storedPrefs,
                            ingredients = displayedAvoid(storedPrefs.profile, state.value.avoidQuery),
                            catalogMeta = indexValue?.meta,
                            freshness = freshness.getOrNull(),
                            failure = (index as? Outcome.Err)?.failure ?: (freshness as? Outcome.Err)?.failure,
                            openReportCount = reportCount,
                            adsRemoved = storedPrefs.adsRemoved
                        )
                    }
                    is Outcome.Err -> state.value = state.value.copy(failure = stored.failure)
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
            ingredients = displayedAvoid(state.value.stored.profile, text)
        )
    }

    fun setFollowSystemLocale() {
        update { current -> current.copy(localePreference = LocalePreference.FOLLOW_SYSTEM, pinnedLocale = null) }
    }

    fun pinLocale(locale: AppLocale) {
        update { current -> current.copy(localePreference = LocalePreference.PINNED, pinnedLocale = locale) }
    }

    fun setFollowSystemTheme() {
        update { current -> current.copy(themePreference = ThemePreference.FOLLOW_SYSTEM) }
    }

    fun setLightTheme() {
        update { current -> current.copy(themePreference = ThemePreference.LIGHT) }
    }

    fun setDarkTheme() {
        update { current -> current.copy(themePreference = ThemePreference.DARK) }
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

    fun requestReset(kind: DataResetKind) {
        state.value = state.value.copy(pendingReset = kind)
    }

    fun cancelReset() {
        state.value = state.value.copy(pendingReset = null)
    }

    fun confirmReset() {
        val kind: DataResetKind = state.value.pendingReset ?: return
        state.value = state.value.copy(pendingReset = null)
        viewModelScope.launch {
            runUiAction(::showFailure) { resetAction(kind) } ?: return@launch
            state.value = state.value.copy(cleared = kind, failure = null)
            if (kind == DataResetKind.DEVICE || kind == DataResetKind.AVOID_LIST) {
                reload()
            }
            if (kind == DataResetKind.DEVICE) {
                adsSession.refresh()
            }
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

    fun copyReports(emptyText: String) {
        viewModelScope.launch {
            val items = runUiAction(::showFailure) { reports.openReports() } ?: return@launch
            val text: String = items.joinToString(separator = "\n") { report ->
                "${report.kind}\t${report.gtin.orEmpty()}\t${report.payloadJson}"
            }
            copyPlainText(text.ifBlank { emptyText })
            state.value = state.value.copy(reportsCopied = true, failure = null)
        }
    }

    fun sendReports() {
        if (!flushReports.isConfigured()) {
            return
        }
        viewModelScope.launch {
            runUiAction(::showFailure) { flushReports.invoke() } ?: return@launch
            state.value = state.value.copy(reportsSent = true, failure = null)
            reload()
        }
    }

    fun copyAvoidList(emptyText: String) {
        val text: String = PreferencesExportText.avoidList(
            avoidedIngredientIds = state.value.stored.profile.avoidedIngredientIds,
            ingredientsById = ingredientsById,
            emptyText = emptyText
        )
        copyPlainText(text)
        state.value = state.value.copy(avoidCopied = true, failure = null)
    }

    fun copyShelf(emptyText: String) {
        viewModelScope.launch {
            val items = runUiAction(::showFailure) { shelf.all() } ?: return@launch
            copyPlainText(PreferencesExportText.shelf(items, emptyText))
            state.value = state.value.copy(shelfCopied = true, failure = null)
        }
    }

    fun purchaseRemoveAds() {
        if (!billing.isAvailable()) {
            return
        }
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

    private suspend fun resetAction(kind: DataResetKind): Outcome<Unit> {
        return when (kind) {
            DataResetKind.HISTORY -> userDataReset.clearHistory()
            DataResetKind.SHELF -> userDataReset.clearShelf()
            DataResetKind.AVOID_LIST -> userDataReset.clearAvoidList()
            DataResetKind.DEVICE -> userDataReset.resetDevice()
        }
    }

    private fun update(transform: (StoredPreferences) -> StoredPreferences) {
        viewModelScope.launch {
            persistMutex.withLock {
                val next: StoredPreferences = transform(state.value.stored)
                val saved = runUiAction(onFailure = ::showFailure) { repository.save(next) }
                if (saved != null) {
                    themeSession.publish(next.themePreference)
                    state.value = state.value.copy(
                        stored = next,
                        ingredients = displayedAvoid(next.profile, state.value.avoidQuery),
                        failure = null
                    )
                }
            }
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.value = state.value.copy(failure = failure)
    }

    private fun displayedAvoid(profile: UserAvoidanceProfile, query: String): List<Ingredient> {
        val needle: String = query.trim()
        if (needle.isEmpty()) {
            return profile.avoidedIngredientIds.mapNotNull { ingredientId -> ingredientsById[ingredientId] }
        }
        return catalogIndex
            ?.searchIngredients(needle)
            ?.take(AVOID_SEARCH_LIMIT)
            .orEmpty()
    }

    private companion object {
        const val AVOID_SEARCH_LIMIT: Int = 40
    }
}
