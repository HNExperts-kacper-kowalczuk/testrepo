package com.hnexperts.cosmetics.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.BarcodeLookup
import com.hnexperts.cosmetics.catalog.application.ResolveBarcode
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.application.ScanBridge
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val busy: Boolean = false,
    val invalidBarcode: Boolean = false,
    val emptyInci: Boolean = false,
    val notFoundGtin: String? = null,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false
)

class ScanViewModel(
    private val resolveBarcode: ResolveBarcode,
    private val evaluateProduct: EvaluateProduct,
    private val scanBridge: ScanBridge
) : ViewModel() {
    private val state: MutableStateFlow<ScanUiState> = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = state.asStateFlow()
    private var runningJob: Job? = null

    init {
        viewModelScope.launch {
            scanBridge.notFoundGtin.collect { gtin ->
                if (gtin != null) {
                    state.update { current ->
                        current.copy(notFoundGtin = gtin, invalidBarcode = false, failure = null)
                    }
                    scanBridge.consumeNotFound()
                }
            }
        }
    }

    fun lookupBarcode(raw: String) {
        startWork {
            when (val lookup: BarcodeLookup = runUiAction(::showFailure) { resolveBarcode.invoke(raw) } ?: return@startWork) {
                is BarcodeLookup.Invalid -> state.update { current ->
                    current.copy(invalidBarcode = true, emptyInci = false, notFoundGtin = null)
                }
                is BarcodeLookup.NotFound -> state.update { current ->
                    current.copy(invalidBarcode = false, notFoundGtin = lookup.gtin)
                }
                is BarcodeLookup.Found -> evaluateAndOpen(
                    inciRaw = lookup.product.inciRaw,
                    source = "barcode",
                    productName = lookup.product.name,
                    brand = lookup.product.brand,
                    gtin = lookup.gtin
                )
            }
        }
    }

    fun evaluateTypedList(inciRaw: String) {
        if (inciRaw.isBlank()) {
            state.update { current -> current.copy(emptyInci = true, invalidBarcode = false, failure = null) }
            return
        }
        startWork {
            evaluateAndOpen(inciRaw = inciRaw, source = "manual")
        }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false) }
    }

    private suspend fun evaluateAndOpen(
        inciRaw: String,
        source: String,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null
    ) {
        runUiAction(onFailure = ::showFailure) {
            evaluateProduct.invoke(
                inciRaw = inciRaw,
                source = source,
                productName = productName,
                brand = brand,
                gtin = gtin
            )
        } ?: return
        state.update { current ->
            current.copy(
                invalidBarcode = false,
                notFoundGtin = null,
                emptyInci = false,
                failure = null,
                navigateToResult = true
            )
        }
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure, navigateToResult = false) }
    }

    private fun startWork(block: suspend () -> Unit) {
        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            state.update { current ->
                current.copy(busy = true, invalidBarcode = false, emptyInci = false, failure = null)
            }
            try {
                block()
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }
}
