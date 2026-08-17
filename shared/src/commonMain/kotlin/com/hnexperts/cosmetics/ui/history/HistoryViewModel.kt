package com.hnexperts.cosmetics.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.scanning.data.HistoryEntry
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val busy: Boolean = false,
    val navigateToResult: Boolean = false
)

class HistoryViewModel(
    private val history: SqlHistoryRepository,
    private val evaluateProduct: EvaluateProduct
) : ViewModel() {
    private val state: MutableStateFlow<HistoryUiState> = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = state.asStateFlow()
    private var openJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            val entries: List<HistoryEntry> = history.recent()
            state.update { current -> current.copy(entries = entries) }
        }
    }

    fun reopen(entry: HistoryEntry) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            state.update { current -> current.copy(busy = true) }
            try {
                evaluateProduct.invoke(
                    inciRaw = entry.inciRaw,
                    source = entry.source,
                    gtin = entry.gtin
                )
                state.update { current -> current.copy(navigateToResult = true) }
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false) }
    }
}
