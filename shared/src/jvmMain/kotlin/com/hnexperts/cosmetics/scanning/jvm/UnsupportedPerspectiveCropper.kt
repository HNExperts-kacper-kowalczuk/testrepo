package com.hnexperts.cosmetics.scanning.jvm

import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad

class UnsupportedPerspectiveCropper : PerspectiveCropper {
    override suspend fun upright(frame: CameraFrame): Outcome<CameraFrame> {
        return unsupported()
    }

    override suspend fun crop(uprightFrame: CameraFrame, quad: SelectionQuad): Outcome<CameraFrame> {
        return unsupported()
    }

    private fun unsupported(): Outcome<CameraFrame> {
        return Outcome.Err(
            AppFailure.Ocr(
                operation = "crop.unsupported",
                detail = "Image cropping is not available on this platform."
            )
        )
    }
}
