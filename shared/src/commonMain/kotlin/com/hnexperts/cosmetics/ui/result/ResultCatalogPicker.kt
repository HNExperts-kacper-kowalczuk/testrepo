package com.hnexperts.cosmetics.ui.result

import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.application.ReplaceUnmatchedIngredient
import com.hnexperts.cosmetics.scanning.application.SuggestReviewIngredients
import com.hnexperts.cosmetics.scanning.domain.IngredientSuggestion
import com.hnexperts.cosmetics.scanning.domain.ReviewSuggestionLists
import com.hnexperts.cosmetics.ui.confirm.ConfirmPickerState
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ResultCatalogPicker(
    private val suggestReviewIngredients: SuggestReviewIngredients,
    private val replaceUnmatched: ReplaceUnmatchedIngredient,
    private val state: MutableStateFlow<ResultUiState>,
    private val scope: CoroutineScope,
    private val rescore: suspend (ProductAssessment, String) -> Unit
) {
    private var suggestJob: Job? = null

    fun open(finding: Finding) {
        if (finding.ingredient.id != null) {
            return
        }
        startLoad(
            picker = ConfirmPickerState(
                tokenKey = finding.listIndex.toLong(),
                rawText = finding.ingredient.displayName,
                busy = true
            ),
            debounceMs = 0L
        )
    }

    fun updateQuery(query: String) {
        val picker: ConfirmPickerState = state.value.picker ?: return
        startLoad(picker = picker.copy(query = query), debounceMs = QUERY_DEBOUNCE_MS)
    }

    fun dismiss() {
        suggestJob?.cancel()
        state.value = state.value.copy(picker = null)
    }

    suspend fun applySuggestion(suggestion: IngredientSuggestion) {
        val picker: ConfirmPickerState = state.value.picker ?: return
        val assessment: ProductAssessment = state.value.assessment ?: return
        suggestJob?.cancel()
        applyPick(assessment, picker.tokenKey.toInt(), suggestion.inciName)
    }

    private suspend fun applyPick(current: ProductAssessment, listIndex: Int, catalogName: String) {
        val replaced: String? = runUiAction(::showFailure) {
            replaceUnmatched.invoke(current.inciRaw, listIndex, catalogName)
        }
        if (replaced == null) {
            state.value = state.value.copy(picker = state.value.picker?.copy(busy = false))
            return
        }
        rescore(current, replaced)
    }

    private fun startLoad(picker: ConfirmPickerState, debounceMs: Long) {
        suggestJob?.cancel()
        state.value = state.value.copy(picker = picker, failure = null, selectedDetail = null)
        suggestJob = scope.launch {
            if (debounceMs > 0L) {
                delay(debounceMs)
            }
            loadSuggestions(picker.tokenKey, picker.rawText, picker.query)
        }
    }

    private suspend fun loadSuggestions(tokenKey: Long, rawText: String, query: String) {
        val lists: ReviewSuggestionLists? = runUiAction(::showFailure) {
            suggestReviewIngredients.invoke(rawText, query)
        }
        val picker: ConfirmPickerState = state.value.picker ?: return
        if (picker.tokenKey != tokenKey || picker.query != query) {
            return
        }
        if (lists == null) {
            state.value = state.value.copy(picker = picker.copy(busy = false))
            return
        }
        state.value = state.value.copy(
            picker = picker.copy(nearby = lists.nearby, search = lists.search, busy = false)
        )
    }

    private fun showFailure(failure: AppFailure) {
        state.value = state.value.copy(failure = failure)
    }

    private companion object {
        const val QUERY_DEBOUNCE_MS: Long = 250L
    }
}
