package com.hnexperts.cosmetics.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.GtinResolution
import com.hnexperts.cosmetics.catalog.application.ResolveGtin
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.platform.performScanHaptic
import com.hnexperts.cosmetics.scanning.application.PendingCaptureSession
import com.hnexperts.cosmetics.scanning.application.ScanBridge
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import com.hnexperts.cosmetics.ui.runUiAction
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraScanUiState(
    val mode: ScannerMode = ScannerMode.BARCODE,
    val torchOn: Boolean = false,
    val permission: CameraPermissionStatus = CameraPermissionStatus.UNKNOWN,
    val captureNonce: Int = 0,
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false,
    val navigateToCrop: Boolean = false,
    val navigateBackNotFound: Boolean = false
)

class CameraScanViewModel(
    private val resolveGtin: ResolveGtin,
    private val evaluateProduct: EvaluateProduct,
    private val pendingCapture: PendingCaptureSession,
    private val scanBridge: ScanBridge,
    initialMode: ScannerMode = ScannerMode.BARCODE
) : ViewModel() {
    private val state: MutableStateFlow<CameraScanUiState> = MutableStateFlow(CameraScanUiState(mode = initialMode))
    val uiState: StateFlow<CameraScanUiState> = state.asStateFlow()
    private val startedAt: TimeMark = TimeSource.Monotonic.markNow()
    private var lastAcceptElapsedMs: Long = -DEBOUNCE_MS
    private var acceptingBarcode: Boolean = false
    private var workJob: Job? = null

    fun setMode(mode: ScannerMode) {
        state.update { current -> current.copy(mode = mode, failure = null) }
    }

    fun onPermission(status: CameraPermissionStatus) {
        state.update { current -> current.copy(permission = status) }
    }

    fun toggleTorch() {
        state.update { current -> current.copy(torchOn = !current.torchOn) }
    }

    fun onCameraFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure, busy = false) }
    }

    fun onBarcode(payload: BarcodePayload) {
        if (state.value.mode != ScannerMode.BARCODE || acceptingBarcode || state.value.busy) {
            return
        }
        if (!passedDebounce()) {
            return
        }
        acceptingBarcode = true
        performScanHaptic()
        workJob?.cancel()
        workJob = viewModelScope.launch {
            state.update { current -> current.copy(busy = true, failure = null) }
            try {
                handleBarcode(payload.gtin)
            } finally {
                val terminal: Boolean = state.value.navigateToResult || state.value.navigateBackNotFound
                if (!terminal) {
                    acceptingBarcode = false
                }
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun captureStill() {
        if (state.value.mode != ScannerMode.INGREDIENT_LIST || state.value.busy) {
            return
        }
        if (state.value.permission != CameraPermissionStatus.GRANTED) {
            return
        }
        state.update { current -> current.copy(captureNonce = current.captureNonce + 1, failure = null) }
    }

    fun onStillCaptured(frame: CameraFrame) {
        pendingCapture.publish(frame)
        performScanHaptic()
        state.update { current -> current.copy(navigateToCrop = true, failure = null) }
    }

    fun consumeNavigation() {
        state.update { current ->
            current.copy(
                navigateToResult = false,
                navigateToCrop = false,
                navigateBackNotFound = false
            )
        }
    }

    private suspend fun handleBarcode(raw: String) {
        val resolution: GtinResolution = runUiAction(::showFailure) { resolveGtin.invoke(raw) } ?: return
        when (resolution) {
            GtinResolution.Invalid -> showFailure(
                AppFailure.Camera(operation = "barcode.invalid", detail = "Decoded value '$raw' is not a GTIN")
            )
            is GtinResolution.Unknown -> {
                scanBridge.publishNotFound(resolution.gtin, resolution.onlineNoIngredients)
                state.update { current -> current.copy(navigateBackNotFound = true) }
            }
            is GtinResolution.ReadyToEvaluate -> {
                runUiAction(::showFailure) {
                    evaluateProduct.invoke(
                        inciRaw = resolution.inciRaw,
                        source = resolution.source,
                        productName = resolution.productName,
                        brand = resolution.brand,
                        gtin = resolution.gtin,
                        usage = resolution.usage
                    )
                } ?: return
                state.update { current -> current.copy(navigateToResult = true) }
            }
        }
    }

    private fun passedDebounce(): Boolean {
        val elapsed: Long = startedAt.elapsedNow().inWholeMilliseconds
        if (elapsed - lastAcceptElapsedMs < DEBOUNCE_MS) {
            return false
        }
        lastAcceptElapsedMs = elapsed
        return true
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure) }
    }

    private companion object {
        const val DEBOUNCE_MS: Long = 800
    }
}
