package com.hnexperts.cosmetics.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.scanning.application.IngredientReviewSession
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import com.hnexperts.cosmetics.scanning.domain.ReviewToken
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmUiState(
    val draft: IngredientReviewDraft? = null,
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false
)

class ConfirmIngredientsViewModel(
    private val reviewSession: IngredientReviewSession,
    private val evaluateProduct: EvaluateProduct
) : ViewModel() {
    private val state: MutableStateFlow<ConfirmUiState> = MutableStateFlow(ConfirmUiState())
    val uiState: StateFlow<ConfirmUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val draft: IngredientReviewDraft? = reviewSession.current()
            if (draft == null) {
                state.update { current ->
                    current.copy(
                        failure = AppFailure.Ocr(
                            operation = "ocr.review.missing",
                            detail = "No captured ingredient list is waiting for confirmation."
                        )
                    )
                }
            } else {
                state.update { current -> current.copy(draft = draft) }
            }
        }
    }

    fun updateRaw(key: Long, rawText: String) {
        replaceToken(key) { token ->
            token.copy(
                rawText = rawText,
                suggestedName = rawText,
                matchedIngredientId = null,
                matchMethod = MatchMethod.UNMATCHED,
                fuzzyDecision = FuzzyDecision.NOT_APPLICABLE
            )
        }
    }

    fun acceptFuzzy(key: Long) {
        replaceToken(key) { token -> token.copy(fuzzyDecision = FuzzyDecision.ACCEPTED) }
    }

    fun rejectFuzzy(key: Long) {
        replaceToken(key) { token ->
            token.copy(
                fuzzyDecision = FuzzyDecision.REJECTED,
                matchedIngredientId = null,
                matchMethod = MatchMethod.UNMATCHED
            )
        }
    }

    fun removeToken(key: Long) {
        mutateDraft { draft -> draft.copy(tokens = draft.tokens.filter { token -> token.key != key }) }
    }

    fun addToken() {
        mutateDraft { draft ->
            val token = ReviewToken(
                key = draft.nextKey,
                rawText = "",
                suggestedName = "",
                matchedIngredientId = null,
                matchMethod = MatchMethod.UNMATCHED,
                fuzzyDecision = FuzzyDecision.NOT_APPLICABLE
            )
            draft.copy(tokens = draft.tokens + token, nextKey = draft.nextKey + 1L)
        }
    }

    fun evaluate() {
        val draft: IngredientReviewDraft = state.value.draft ?: return
        if (draft.hasPendingFuzzy()) {
            state.update { current ->
                current.copy(
                    failure = AppFailure.Ocr(
                        operation = "ocr.review.fuzzy",
                        detail = "Accept or reject each fuzzy match before evaluating."
                    )
                )
            }
            return
        }
        val inciRaw: String = draft.toInciRaw()
        if (inciRaw.isBlank()) {
            state.update { current ->
                current.copy(
                    failure = AppFailure.Ocr(
                        operation = "ocr.review.empty",
                        detail = "Add at least one ingredient name."
                    )
                )
            }
            return
        }
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true, failure = null) }
            try {
                runUiAction(::showFailure) {
                    evaluateProduct.invoke(inciRaw = inciRaw, source = "ocr")
                } ?: return@launch
                state.update { current -> current.copy(navigateToResult = true) }
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false) }
    }

    private fun replaceToken(key: Long, transform: (ReviewToken) -> ReviewToken) {
        mutateDraft { draft ->
            draft.copy(tokens = draft.tokens.map { token -> if (token.key == key) transform(token) else token })
        }
    }

    private fun mutateDraft(transform: (IngredientReviewDraft) -> IngredientReviewDraft) {
        state.update { current ->
            val draft: IngredientReviewDraft = current.draft ?: return@update current
            current.copy(draft = transform(draft), failure = null)
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure) }
    }
}
