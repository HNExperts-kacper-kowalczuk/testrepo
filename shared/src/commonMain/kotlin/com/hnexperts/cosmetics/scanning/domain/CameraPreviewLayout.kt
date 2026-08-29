package com.hnexperts.cosmetics.scanning.domain

/**
 * The live preview surface is usable only after the host view has a
 * non-zero size. Binding CameraX or sizing an AVCapture layer at 0×0
 * leaves a black finder while still capture can still succeed.
 */
object CameraPreviewLayout {
    fun isReady(width: Int, height: Int): Boolean {
        return width > 0 && height > 0
    }
}
