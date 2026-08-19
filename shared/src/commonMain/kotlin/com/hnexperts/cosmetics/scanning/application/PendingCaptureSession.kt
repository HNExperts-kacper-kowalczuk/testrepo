package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.scanning.domain.CameraFrame

/**
 * Hands the captured still from the camera screen to the crop screen.
 * Frames are too large to travel through navigation arguments.
 */
class PendingCaptureSession {
    private var frame: CameraFrame? = null

    fun publish(captured: CameraFrame) {
        frame = captured
    }

    fun peek(): CameraFrame? {
        return frame
    }

    fun clear() {
        frame = null
    }
}
