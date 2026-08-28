package com.hnexperts.cosmetics.ui.motion

import kotlin.test.Test
import kotlin.test.assertEquals

class MotionTest {
    @Test
    fun reduceMotionZeroesDurations() {
        assertEquals(0, Motion.millis(reduceMotion = true, durationMs = Motion.MEDIUM_MS))
        assertEquals(Motion.SHORT_MS, Motion.millis(reduceMotion = false, durationMs = Motion.SHORT_MS))
    }
}
