package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.failure.Outcome

/**
 * Platform image geometry for the ingredient-list crop step.
 *
 * [upright] bakes the capture rotation (or EXIF orientation) into the pixels
 * so the crop screen and the warp share one coordinate system.
 * [crop] perspective-warps the [SelectionQuad] region of an upright frame
 * into a flat rectangle that OCR can read.
 */
interface PerspectiveCropper {
    suspend fun upright(frame: CameraFrame): Outcome<CameraFrame>

    suspend fun crop(uprightFrame: CameraFrame, quad: SelectionQuad): Outcome<CameraFrame>
}
