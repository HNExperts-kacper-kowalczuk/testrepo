package com.hnexperts.cosmetics.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.application.CompareCandidate
import com.hnexperts.cosmetics.evaluation.application.CompareFormulas
import com.hnexperts.cosmetics.evaluation.application.CompareSession
import com.hnexperts.cosmetics.evaluation.application.CompareSummary
import com.hnexperts.cosmetics.evaluation.application.ComparedProduct
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CompareUiState(
    val summary: CompareSummary? = null,
    val failure: AppFailure? = null
)

class CompareViewModel(
    private val session: CompareSession,
    private val catalog: CatalogGateway,
    private val preferences: PreferencesStore,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    private val state: MutableStateFlow<CompareUiState> = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val candidates: List<CompareCandidate> = session.current()
        if (candidates.size < 2) {
            state.value = CompareUiState()
            return
        }
        val index: CatalogIndex = runUiAction(::showFailure) { catalog.awaitIndex() } ?: return
        val profile: UserAvoidanceProfile = when (val loaded: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Err -> {
                showFailure(loaded.failure)
                return
            }
            is Outcome.Ok -> loaded.value.profile
        }
        val assessments: List<ProductAssessment> = withContext(dispatchers.computation) {
            candidates.map { candidate ->
                index.evaluateFormula.evaluate(
                    inciRaw = candidate.inciRaw,
                    profile = profile,
                    productName = candidate.productName,
                    brand = candidate.brand,
                    gtin = candidate.gtin,
                    usage = candidate.usage
                )
            }
        }
        val labelled: List<ComparedProduct> = assessments.mapIndexed { indexValue, assessment ->
            ComparedProduct(
                label = candidates[indexValue].productName
                    ?: candidates[indexValue].gtin
                    ?: assessment.productName
                    ?: "Product ${indexValue + 1}",
                assessment = assessment
            )
        }
        state.value = CompareUiState(summary = CompareFormulas.summarize(labelled), failure = null)
    }

    private fun showFailure(failure: AppFailure) {
        state.value = CompareUiState(failure = failure)
    }
}
