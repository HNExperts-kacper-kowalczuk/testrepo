package com.hnexperts.cosmetics.ui.crop

/** Image letterbox inside a stage, matching Compose `ContentScale.Fit`. */
data class FittedImageRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
}

object CropQuadGeometry {
    fun fittedRect(containerWidth: Float, containerHeight: Float, imageWidth: Int, imageHeight: Int): FittedImageRect {
        if (containerWidth <= 0f || containerHeight <= 0f || imageWidth <= 0 || imageHeight <= 0) {
            return FittedImageRect(0f, 0f, 0f, 0f)
        }
        val scale: Float = minOf(
            containerWidth / imageWidth.toFloat(),
            containerHeight / imageHeight.toFloat()
        )
        val width: Float = imageWidth * scale
        val height: Float = imageHeight * scale
        return FittedImageRect(
            left = (containerWidth - width) / 2f,
            top = (containerHeight - height) / 2f,
            width = width,
            height = height
        )
    }

    fun toNormalized(x: Float, y: Float, rect: FittedImageRect): Pair<Float, Float> {
        if (rect.width <= 0f || rect.height <= 0f) {
            return Pair(0.5f, 0.5f)
        }
        return Pair(
            (x - rect.left) / rect.width,
            (y - rect.top) / rect.height
        )
    }

    fun toCanvas(normalizedX: Float, normalizedY: Float, rect: FittedImageRect): Pair<Float, Float> {
        return Pair(
            rect.left + normalizedX * rect.width,
            rect.top + normalizedY * rect.height
        )
    }
}
