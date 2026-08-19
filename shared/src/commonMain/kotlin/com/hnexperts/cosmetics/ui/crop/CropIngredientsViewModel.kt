package com.hnexperts.cosmetics.ui.crop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.application.IngredientReviewSession
import com.hnexperts.cosmetics.scanning.application.PendingCaptureSession
import com.hnexperts.cosmetics.scanning.application.PrepareIngredientReview
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CornerPoint
import com.hnexperts.cosmetics.scanning.domain.IngredientBlockExtractor
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.OcrDocument
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.domain.QuadCorner
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CropUiState(
    val previewJpeg: ByteArray? = null,
    val quad: SelectionQuad = SelectionQuad.defaultInset(),
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToConfirm: Boolean = false,
    val missingCapture: Boolean = false
)

class CropIngredientsViewModel(
    private val pendingCapture: PendingCaptureSession,
    private val cropper: PerspectiveCropper,
    private val recognizer: IngredientListRecognizer,
    private val prepareReview: PrepareIngredientReview,
    private val reviewSession: IngredientReviewSession
) : ViewModel() {
    private val state: MutableStateFlow<CropUiState> = MutableStateFlow(CropUiState())
    val uiState: StateFlow<CropUiState> = state.asStateFlow()
    private var uprightFrame: CameraFrame? = null

    init {
        loadCapture()
    }

    fun updateCorner(corner: QuadCorner, position: CornerPoint) {
        state.update { current -> current.copy(quad = current.quad.withCorner(corner, position)) }
    }

    fun nudgeCorner(corner: QuadCorner, deltaX: Float, deltaY: Float, rectWidth: Float, rectHeight: Float) {
        if (rectWidth <= 1f || rectHeight <= 1f) {
            return
        }
        val current: CornerPoint = state.value.quad.corner(corner)
        updateCorner(
            corner,
            CornerPoint(
                x = current.x + deltaX / rectWidth,
                y = current.y + deltaY / rectHeight
            )
        )
    }

    fun resetQuad() {
        state.update { current -> current.copy(quad = SelectionQuad.defaultInset()) }
    }

    fun useSelection() {
        val upright: CameraFrame = uprightFrame ?: return
        if (state.value.busy) {
            return
        }
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true, failure = null) }
            try {
                recognizeSelection(upright)
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun abandonCapture() {
        pendingCapture.clear()
    }

    fun consumeNavigation() {
        state.update { current -> current.copy(navigateToConfirm = false) }
    }

    private fun loadCapture() {
        val frame: CameraFrame? = pendingCapture.peek()
        if (frame == null) {
            state.update { current -> current.copy(missingCapture = true) }
            return
        }
        viewModelScope.launch {
            state.update { current -> current.copy(busy = true) }
            try {
                val upright: CameraFrame = runUiAction(::showFailure) { cropper.upright(frame) } ?: return@launch
                uprightFrame = upright
                state.update { current -> current.copy(previewJpeg = upright.bytes) }
            } finally {
                state.update { current -> current.copy(busy = false) }
            }
        }
    }

    private suspend fun recognizeSelection(upright: CameraFrame) {
        val cropped: CameraFrame = runUiAction(::showFailure) {
            cropper.crop(upright, state.value.quad)
        } ?: return
        val document: OcrDocument = runUiAction(::showFailure) { recognizer.recognize(cropped) } ?: return
        val block: String = IngredientBlockExtractor.extract(document.rawText)
        val draft = runUiAction(::showFailure) { prepareReview.invoke(block) } ?: return
        reviewSession.publish(draft)
        pendingCapture.clear()
        state.update { current -> current.copy(navigateToConfirm = true) }
    }

    private fun showFailure(failure: AppFailure) {
        state.update { current -> current.copy(failure = failure) }
    }
}
