package com.hnexperts.cosmetics.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.application.CompareCandidate
import com.hnexperts.cosmetics.evaluation.application.CompareSession
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import com.hnexperts.cosmetics.shelf.application.WatchShelfFormulas
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.shelf.domain.UserShelf
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val shelf: List<ShelfItem> = emptyList(),
    val selectedHistoryIds: Set<Long> = emptySet(),
    val selectedShelfKeys: Set<String> = emptySet(),
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false,
    val navigateToCompare: Boolean = false,
    val formulaChangedKeys: Set<String> = emptySet()
) {
    val compareCount: Int
        get() = selectedHistoryIds.size + selectedShelfKeys.size

    val canCompare: Boolean
        get() = compareCount in 2..3
}

class HistoryViewModel(
    private val history: ScanHistoryRepository,
    private val evaluateProduct: EvaluateProduct,
    private val shelf: UserShelf,
    private val compareSession: CompareSession,
    private val watchFormulas: WatchShelfFormulas
) : ViewModel() {
    private val state: MutableStateFlow<HistoryUiState> = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = state.asStateFlow()
    private var openJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            val entries: List<HistoryEntry>? = runUiAction(onFailure = ::showFailure) {
                history.recent()
            }
            val saved: List<ShelfItem>? = runUiAction(onFailure = ::showFailure) {
                shelf.all()
            }
            val changedKeys: Set<String>? = if (saved == null) {
                emptySet()
            } else {
                runUiAction(onFailure = ::showFailure) { watchFormulas.changedKeys(saved) }
            }
            state.update { current ->
                current.copy(
                    entries = entries ?: current.entries,
                    shelf = saved ?: current.shelf,
                    formulaChangedKeys = changedKeys ?: current.formulaChangedKeys,
                    failure = if (entries != null && saved != null && changedKeys != null) {
                        null
                    } else {
                        current.failure
                    }
                )
            }
        }
    }

    fun toggleHistorySelection(entry: HistoryEntry) {
        state.update { current ->
            current.copy(selectedHistoryIds = toggleId(current.selectedHistoryIds, entry.id, current.compareCount))
        }
    }

    fun toggleShelfSelection(item: ShelfItem) {
        state.update { current ->
            current.copy(selectedShelfKeys = toggleKey(current.selectedShelfKeys, item.shelfKey, current.compareCount))
        }
    }

    fun compareSelected(unnamedFormat: String) {
        val snapshot: HistoryUiState = state.value
        if (!snapshot.canCompare) {
            return
        }
        val candidates: List<CompareCandidate> = selectedCandidates(snapshot)
        compareSession.publish(candidates, unnamedFormat)
        state.update { current -> current.copy(navigateToCompare = true) }
    }

    fun reopen(entry: HistoryEntry) {
        open(
            inciRaw = entry.inciRaw,
            source = entry.source,
            gtin = entry.gtin,
            productName = entry.name,
            brand = entry.brand,
            usage = entry.usage,
            productId = entry.productId,
            category = entry.category
        )
    }

    fun reopenShelf(item: ShelfItem) {
        open(
            inciRaw = item.inciRaw,
            source = "shelf",
            gtin = item.gtin,
            productName = item.name,
            brand = item.brand,
            usage = item.usage,
            productId = item.productId,
            category = item.category
        )
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false, navigateToCompare = false) }
    }

    private fun open(
        inciRaw: String,
        source: String,
        gtin: String?,
        productName: String? = null,
        brand: String? = null,
        usage: ProductUsage = ProductUsage.UNKNOWN,
        productId: String? = null,
        category: String? = null
    ) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            state.update { current -> current.copy(busy = true, failure = null) }
            try {
                val assessment = runUiAction(onFailure = ::showFailure) {
                    evaluateProduct.invoke(
                        inciRaw = inciRaw,
                        source = source,
                        productName = productName,
                        brand = brand,
                        gtin = gtin,
                        usage = usage,
                        productId = productId,
                        category = category
                    )
                }
                if (assessment != null) {
                    state.update { current -> current.copy(navigateToResult = true) }
                }
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    private fun selectedCandidates(snapshot: HistoryUiState): List<CompareCandidate> {
        val fromHistory: List<CompareCandidate> = snapshot.entries
            .filter { entry -> snapshot.selectedHistoryIds.contains(entry.id) }
            .map { entry ->
                CompareCandidate(
                    inciRaw = entry.inciRaw,
                    productName = entry.name,
                    brand = entry.brand,
                    gtin = entry.gtin,
                    usage = entry.usage,
                    productId = entry.productId,
                    category = entry.category
                )
            }
        val fromShelf: List<CompareCandidate> = snapshot.shelf
            .filter { item -> snapshot.selectedShelfKeys.contains(item.shelfKey) }
            .map { item ->
                CompareCandidate(
                    inciRaw = item.inciRaw,
                    productName = item.name,
                    brand = item.brand,
                    gtin = item.gtin,
                    usage = item.usage,
                    productId = item.productId,
                    category = item.category
                )
            }
        return fromHistory + fromShelf
    }

    private fun toggleId(current: Set<Long>, id: Long, selectedCount: Int): Set<Long> {
        if (current.contains(id)) {
            return current - id
        }
        if (selectedCount >= 3) {
            return current
        }
        return current + id
    }

    private fun toggleKey(current: Set<String>, key: String, selectedCount: Int): Set<String> {
        if (current.contains(key)) {
            return current - key
        }
        if (selectedCount >= 3) {
            return current
        }
        return current + key
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current ->
            current.copy(failure = failure, navigateToResult = false, navigateToCompare = false)
        }
    }
}
