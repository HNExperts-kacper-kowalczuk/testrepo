package com.hnexperts.cosmetics.scanning.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanBridge {
    private val notFound: MutableStateFlow<String?> = MutableStateFlow(null)
    val notFoundGtin: StateFlow<String?> = notFound.asStateFlow()

    fun publishNotFound(gtin: String) {
        notFound.value = gtin
    }

    fun consumeNotFound(): String? {
        val current: String? = notFound.value
        notFound.value = null
        return current
    }
}
