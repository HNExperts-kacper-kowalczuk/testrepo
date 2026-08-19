package com.hnexperts.cosmetics.ui.crop

import kotlin.test.Test
import kotlin.test.assertEquals

class CropQuadGeometryTest {
    @Test
    fun letterboxesAPortraitPhotoInAWideStage() {
        val rect: FittedImageRect = CropQuadGeometry.fittedRect(
            containerWidth = 1000f,
            containerHeight = 500f,
            imageWidth = 100,
            imageHeight = 200
        )
        assertEquals(250f, rect.width, 0.01f)
        assertEquals(500f, rect.height, 0.01f)
        assertEquals(375f, rect.left, 0.01f)
        assertEquals(0f, rect.top, 0.01f)
    }

    @Test
    fun roundTripsACornerThroughCanvasAndNormalizedSpace() {
        val rect: FittedImageRect = CropQuadGeometry.fittedRect(400f, 800f, 400, 800)
        val (x, y) = CropQuadGeometry.toCanvas(0.1f, 0.9f, rect)
        val (nx, ny) = CropQuadGeometry.toNormalized(x, y, rect)
        assertEquals(0.1f, nx, 0.001f)
        assertEquals(0.9f, ny, 0.001f)
    }
}
