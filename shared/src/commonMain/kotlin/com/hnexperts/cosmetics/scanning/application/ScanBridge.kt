package com.hnexperts.cosmetics.scanning.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UnknownGtinNotice(
    val gtin: String,
    val onlineNoIngredients: Boolean
)

class ScanBridge {
    private val notFound: MutableStateFlow<UnknownGtinNotice?> = MutableStateFlow(null)
    val unknownGtin: StateFlow<UnknownGtinNotice?> = notFound.asStateFlow()

    fun publishNotFound(gtin: String, onlineNoIngredients: Boolean) {
        notFound.value = UnknownGtinNotice(gtin, onlineNoIngredients)
    }

    fun consumeNotFound(): UnknownGtinNotice? {
        val current: UnknownGtinNotice? = notFound.value
        notFound.value = null
        return current
    }
}
