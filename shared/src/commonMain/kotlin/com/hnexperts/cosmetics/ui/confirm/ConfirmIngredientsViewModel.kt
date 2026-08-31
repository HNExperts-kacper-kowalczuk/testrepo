package com.hnexperts.cosmetics.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.scanning.application.IngredientReviewSession
import com.hnexperts.cosmetics.scanning.application.PendingVerifySession
import com.hnexperts.cosmetics.scanning.application.SuggestReviewIngredients
import com.hnexperts.cosmetics.scanning.application.VerifyRequest
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.InciTokenSet
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import com.hnexperts.cosmetics.scanning.domain.IngredientSuggestion
import com.hnexperts.cosmetics.scanning.domain.ReportKinds
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.scanning.domain.ReviewDraftMerger
import com.hnexperts.cosmetics.scanning.domain.ReviewSuggestionLists
import com.hnexperts.cosmetics.scanning.domain.ReviewToken
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmPickerState(
    val tokenKey: Long,
    val rawText: String,
    val query: String = "",
    val nearby: List<IngredientSuggestion> = emptyList(),
    val search: List<IngredientSuggestion> = emptyList(),
    val busy: Boolean = false
)

data class ConfirmUiState(
    val draft: IngredientReviewDraft? = null,
    val usage: ProductUsage = ProductUsage.LEAVE_ON,
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val photoCount: Int = 1,
    val canAddPhoto: Boolean = true,
    val navigateToResult: Boolean = false,
    val navigateToCamera: Boolean = false,
    val picker: ConfirmPickerState? = null
)

class ConfirmIngredientsViewModel(
    private val reviewSession: IngredientReviewSession,
    private val pendingVerify: PendingVerifySession,
    private val evaluateProduct: EvaluateProduct,
    private val reports: ReportQueue,
    private val suggestReviewIngredients: SuggestReviewIngredients
) : ViewModel() {
    private val state: MutableStateFlow<ConfirmUiState> = MutableStateFlow(ConfirmUiState())
    val uiState: StateFlow<ConfirmUiState> = state.asStateFlow()
    private var suggestJob: Job? = null

    init {
        viewModelScope.launch { loadDraft() }
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

    fun changeAutoFilled(key: Long) {
        replaceToken(key) { token -> token.copy(fuzzyDecision = FuzzyDecision.PENDING) }
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

    fun openPicker(key: Long) {
        val token: ReviewToken = tokenByKey(key) ?: return
        if (!token.canPickFromCatalog()) {
            return
        }
        startSuggestionLoad(
            picker = ConfirmPickerState(tokenKey = key, rawText = token.rawText, busy = true),
            debounceMs = 0L
        )
    }

    fun updatePickerQuery(query: String) {
        val picker: ConfirmPickerState = state.value.picker ?: return
        startSuggestionLoad(
            picker = picker.copy(query = query),
            debounceMs = QUERY_DEBOUNCE_MS
        )
    }

    fun pickSuggestion(suggestion: IngredientSuggestion) {
        val picker: ConfirmPickerState = state.value.picker ?: return
        cancelSuggestions()
        replaceToken(picker.tokenKey) { token -> token.withCatalogPick(suggestion.id, suggestion.inciName) }
        state.update { current -> current.copy(picker = null) }
    }

    fun dismissPicker() {
        cancelSuggestions()
        state.update { current -> current.copy(picker = null) }
    }

    fun setUsage(usage: ProductUsage) {
        state.update { current -> current.copy(usage = usage, failure = null) }
    }

    fun addAnotherPhoto() {
        val draft: IngredientReviewDraft = state.value.draft ?: return
        if (state.value.photoCount >= ReviewDraftMerger.MAX_SHOTS) {
            return
        }
        viewModelScope.launch {
            pendingVerify.stashDraft(draft)
            state.update { current -> current.copy(navigateToCamera = true) }
        }
    }

    fun evaluate() {
        val draft: IngredientReviewDraft = state.value.draft ?: return
        if (draft.hasPendingFuzzy() || draft.toInciRaw().isBlank()) {
            state.update { current -> current.copy(failure = reviewProblem(draft)) }
            return
        }
        viewModelScope.launch { evaluateCaptured(draft) }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false, navigateToCamera = false) }
    }

    private suspend fun loadDraft() {
        val stashed: IngredientReviewDraft? = pendingVerify.takeStashedDraft()
        val incoming: IngredientReviewDraft? = reviewSession.current()
        val draft: IngredientReviewDraft? = when {
            stashed != null && incoming != null -> ReviewDraftMerger.merge(stashed, incoming)
            incoming != null -> incoming
            else -> null
        }
        if (draft == null) {
            state.update { current ->
                current.copy(
                    failure = AppFailure.Ocr(
                        operation = "ocr.review.missing",
                        detail = "No captured ingredient list is waiting for confirmation."
                    )
                )
            }
            return
        }
        val photos: Int = pendingVerify.notePhoto()
        val verify: VerifyRequest? = pendingVerify.currentVerify()
        state.update { current ->
            current.copy(
                draft = draft,
                usage = draft.usage ?: verify?.usage ?: current.usage,
                photoCount = photos,
                canAddPhoto = photos < ReviewDraftMerger.MAX_SHOTS
            )
        }
    }

    private suspend fun evaluateCaptured(draft: IngredientReviewDraft) {
        state.update { current -> current.copy(busy = true, failure = null) }
        try {
            val photographed: String = draft.toInciRaw()
            val verify: VerifyRequest? = pendingVerify.currentVerify()
            val gtin: String? = verify?.gtin ?: pendingVerify.unknownGtin()
            val catalogInci: String? = verify?.catalogInci
            val matched: Boolean = catalogInci != null && InciTokenSet.equal(photographed, catalogInci)
            runUiAction(::showFailure) {
                evaluateProduct.invoke(
                    inciRaw = if (matched && catalogInci != null) catalogInci else photographed,
                    source = if (matched && verify != null) verify.source else draft.source,
                    productName = verify?.productName,
                    brand = verify?.brand,
                    gtin = gtin,
                    usage = state.value.usage,
                    packVerified = matched
                )
            } ?: return
            if (verify != null && !matched) {
                reports.enqueue(
                    CatalogReport(
                        kind = ReportKinds.WRONG_INCI,
                        gtin = verify.gtin,
                        payloadJson = "{\"catalog\":true}"
                    )
                )
            }
            if (gtin != null) {
                reports.attachPayload(gtin, ReportKinds.MISSING_PRODUCT, photographed)
            }
            pendingVerify.clearVerify()
            state.update { current -> current.copy(navigateToResult = true) }
        } finally {
            state.update { current -> current.copy(busy = false) }
        }
    }

    private fun reviewProblem(draft: IngredientReviewDraft): AppFailure {
        return if (draft.hasPendingFuzzy()) {
            AppFailure.Ocr(
                operation = "ocr.review.fuzzy",
                detail = "Accept or reject each fuzzy match before evaluating."
            )
        } else {
            AppFailure.Ocr(operation = "ocr.review.empty", detail = "Add at least one ingredient name.")
        }
    }

    private fun startSuggestionLoad(picker: ConfirmPickerState, debounceMs: Long) {
        cancelSuggestions()
        state.update { current -> current.copy(picker = picker, failure = null) }
        suggestJob = viewModelScope.launch {
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
        state.update { current -> applySuggestionLists(current, tokenKey, query, lists) }
    }

    private fun applySuggestionLists(
        current: ConfirmUiState,
        tokenKey: Long,
        query: String,
        lists: ReviewSuggestionLists?
    ): ConfirmUiState {
        val picker: ConfirmPickerState = current.picker ?: return current
        if (picker.tokenKey != tokenKey || picker.query != query) {
            return current
        }
        if (lists == null) {
            return current.copy(picker = picker.copy(busy = false))
        }
        return current.copy(
            picker = picker.copy(
                nearby = lists.nearby,
                search = lists.search,
                busy = false
            )
        )
    }

    private fun tokenByKey(key: Long): ReviewToken? {
        return state.value.draft?.tokens?.firstOrNull { token -> token.key == key }
    }

    private fun cancelSuggestions() {
        suggestJob?.cancel()
        suggestJob = null
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

    private companion object {
        const val QUERY_DEBOUNCE_MS: Long = 250L
    }
}
