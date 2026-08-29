package com.hnexperts.cosmetics.scanning.ios

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

/**
 * Compose no longer calls UIKitView onResize. The preview layer must
 * follow this view in layoutSubviews or it stays 0×0 (black finder).
 */
@OptIn(ExperimentalForeignApi::class)
class IosPreviewContainer : UIView(frame = CGRectZero.readValue()) {
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun layoutSubviews() {
        super.layoutSubviews()
        syncLayerFrame()
    }

    fun syncLayerFrame() {
        val layer: AVCaptureVideoPreviewLayer = previewLayer ?: return
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        layer.setFrame(bounds)
        CATransaction.commit()
    }
}
