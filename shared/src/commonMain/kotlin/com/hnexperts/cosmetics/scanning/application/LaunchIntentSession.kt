package com.hnexperts.cosmetics.scanning.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LaunchIntentSession {
    private val barcodeRequested: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val openBarcodeCamera: StateFlow<Boolean> = barcodeRequested.asStateFlow()

    fun requestBarcodeCamera() {
        barcodeRequested.value = true
    }

    fun consume() {
        barcodeRequested.value = false
    }
}
