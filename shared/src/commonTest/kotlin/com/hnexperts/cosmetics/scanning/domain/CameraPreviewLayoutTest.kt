package com.hnexperts.cosmetics.scanning.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CameraPreviewLayoutTest {
    @Test
    fun zeroOrNegativeSizeIsNotReady() {
        assertFalse(CameraPreviewLayout.isReady(0, 0))
        assertFalse(CameraPreviewLayout.isReady(320, 0))
        assertFalse(CameraPreviewLayout.isReady(0, 240))
        assertFalse(CameraPreviewLayout.isReady(-1, 100))
    }

    @Test
    fun positiveSizeIsReady() {
        assertTrue(CameraPreviewLayout.isReady(1, 1))
        assertTrue(CameraPreviewLayout.isReady(390, 520))
    }
}
