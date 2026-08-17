package com.hnexperts.cosmetics.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
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
    val navigateToResult: Boolean = false
)

class ScanViewModel(
    private val products: SqlProductRepository,
    private val evaluateProduct: EvaluateProduct
) : ViewModel() {
    private val state: MutableStateFlow<ScanUiState> = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = state.asStateFlow()
    private var runningJob: Job? = null

    fun lookupBarcode(raw: String) {
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            state.update { current ->
                current.copy(invalidBarcode = true, emptyInci = false, notFoundGtin = null)
            }
            return
        }
        startWork {
            val product: Product? = products.findByGtin(gtin)
            if (product == null) {
                state.update { current ->
                    current.copy(invalidBarcode = false, notFoundGtin = gtin)
                }
                return@startWork
            }
            evaluateProduct.invoke(
                inciRaw = product.inciRaw,
                source = "barcode",
                productName = product.name,
                brand = product.brand,
                gtin = gtin
            )
            state.update { current ->
                current.copy(
                    invalidBarcode = false,
                    notFoundGtin = null,
                    navigateToResult = true
                )
            }
        }
    }

    fun evaluateTypedList(inciRaw: String) {
        if (inciRaw.isBlank()) {
            state.update { current -> current.copy(emptyInci = true, invalidBarcode = false) }
            return
        }
        startWork {
            evaluateProduct.invoke(inciRaw = inciRaw, source = "manual")
            state.update { current ->
                current.copy(emptyInci = false, navigateToResult = true)
            }
        }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false) }
    }

    private fun startWork(block: suspend () -> Unit) {
        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            state.update { current ->
                current.copy(busy = true, invalidBarcode = false, emptyInci = false)
            }
            try {
                block()
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }
}
