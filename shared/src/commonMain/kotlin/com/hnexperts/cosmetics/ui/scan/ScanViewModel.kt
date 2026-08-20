package com.hnexperts.cosmetics.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.GtinResolution
import com.hnexperts.cosmetics.catalog.application.ResolveGtin
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.application.PendingVerifySession
import com.hnexperts.cosmetics.scanning.application.ScanBridge
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.scanning.domain.ReportKinds
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
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
    val onlineNoIngredients: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false,
    val recent: List<HistoryEntry> = emptyList()
)

class ScanViewModel(
    private val resolveGtin: ResolveGtin,
    private val evaluateProduct: EvaluateProduct,
    private val scanBridge: ScanBridge,
    private val history: ScanHistoryRepository,
    private val reports: ReportQueue,
    private val pendingVerify: PendingVerifySession
) : ViewModel() {
    private val state: MutableStateFlow<ScanUiState> = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = state.asStateFlow()
    private var runningJob: Job? = null

    init {
        viewModelScope.launch {
            scanBridge.unknownGtin.collect { notice ->
                if (notice != null) {
                    state.update { current ->
                        current.copy(
                            notFoundGtin = notice.gtin,
                            onlineNoIngredients = notice.onlineNoIngredients,
                            invalidBarcode = false,
                            failure = null
                        )
                    }
                    scanBridge.consumeNotFound()
                }
            }
        }
    }

    fun lookupBarcode(raw: String) {
        startWork {
            when (val resolution: GtinResolution = runUiAction(::showFailure) { resolveGtin.invoke(raw) } ?: return@startWork) {
                GtinResolution.Invalid -> state.update { current ->
                    current.copy(
                        invalidBarcode = true,
                        emptyInci = false,
                        notFoundGtin = null,
                        onlineNoIngredients = false
                    )
                }
                is GtinResolution.Unknown -> {
                    recordUnknown(resolution)
                    state.update { current ->
                        current.copy(
                            invalidBarcode = false,
                            notFoundGtin = resolution.gtin,
                            onlineNoIngredients = resolution.onlineNoIngredients
                        )
                    }
                }
                is GtinResolution.ReadyToEvaluate -> evaluateReady(resolution)
            }
        }
    }

    fun evaluateTypedList(inciRaw: String, usage: ProductUsage = ProductUsage.UNKNOWN) {
        if (inciRaw.isBlank()) {
            state.update { current -> current.copy(emptyInci = true, invalidBarcode = false, failure = null) }
            return
        }
        startWork {
            evaluateAndOpen(inciRaw = inciRaw, source = "manual", usage = usage)
        }
    }

    fun refreshRecent() {
        viewModelScope.launch {
            val entries: List<HistoryEntry>? = runUiAction(::showFailure) { history.recent() }
            if (entries != null) {
                state.update { current -> current.copy(recent = entries.take(RECENT_LIMIT)) }
            }
        }
    }

    fun reopen(entry: HistoryEntry) {
        startWork {
            evaluateAndOpen(
                inciRaw = entry.inciRaw,
                source = entry.source,
                productName = entry.name,
                brand = entry.brand,
                gtin = entry.gtin,
                usage = entry.usage,
                category = entry.category,
                productId = entry.productId
            )
        }
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToResult = false) }
    }

    private suspend fun evaluateReady(ready: GtinResolution.ReadyToEvaluate) {
        evaluateAndOpen(
            inciRaw = ready.inciRaw,
            source = ready.source,
            productName = ready.productName,
            brand = ready.brand,
            gtin = ready.gtin,
            usage = ready.usage,
            category = ready.category,
            productId = ready.productId
        )
    }

    private suspend fun evaluateAndOpen(
        inciRaw: String,
        source: String,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null,
        usage: ProductUsage = ProductUsage.UNKNOWN,
        category: String? = null,
        productId: String? = null
    ) {
        runUiAction(onFailure = ::showFailure) {
            evaluateProduct.invoke(
                inciRaw = inciRaw,
                source = source,
                productName = productName,
                brand = brand,
                gtin = gtin,
                usage = usage,
                category = category,
                productId = productId
            )
        } ?: return
        state.update { current ->
            current.copy(
                invalidBarcode = false,
                notFoundGtin = null,
                onlineNoIngredients = false,
                emptyInci = false,
                failure = null,
                navigateToResult = true
            )
        }
    }

    private suspend fun recordUnknown(resolution: GtinResolution.Unknown) {
        pendingVerify.rememberUnknownGtin(resolution.gtin)
        reports.enqueue(
            CatalogReport(
                kind = ReportKinds.MISSING_PRODUCT,
                gtin = resolution.gtin,
                payloadJson = "{}"
            )
        )
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure, navigateToResult = false) }
    }

    private fun startWork(block: suspend () -> Unit) {
        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            state.update { current ->
                current.copy(
                    busy = true,
                    invalidBarcode = false,
                    emptyInci = false,
                    failure = null,
                    notFoundGtin = null,
                    onlineNoIngredients = false
                )
            }
            try {
                block()
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    private companion object {
        const val RECENT_LIMIT: Int = 5
    }
}
