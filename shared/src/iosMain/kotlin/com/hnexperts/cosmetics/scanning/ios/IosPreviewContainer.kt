package com.hnexperts.cosmetics.scanning.ios

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIView

/**
 * Compose composites UIKit interop offscreen, so a hardware preview layer
 * stays black unless this view owns layout and is placed as an overlay.
 * Official CMP camera samples size both the host layer and the preview layer.
 */
@OptIn(ExperimentalForeignApi::class)
class IosPreviewContainer : UIView(frame = CGRectZero.readValue()) {
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    init {
        opaque = true
        clipsToBounds = true
        backgroundColor = UIColor.blackColor
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        syncLayerFrame()
    }

    override fun setFrame(frame: CValue<CGRect>) {
        super.setFrame(frame)
        syncLayerFrame()
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()
        syncLayerFrame()
    }

    fun syncLayerFrame() {
        val preview: AVCaptureVideoPreviewLayer = previewLayer ?: return
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        layer.setFrame(frame)
        preview.setFrame(bounds)
        CATransaction.commit()
    }
}
