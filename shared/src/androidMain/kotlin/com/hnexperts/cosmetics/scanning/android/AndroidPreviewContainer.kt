package com.hnexperts.cosmetics.scanning.android

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.view.PreviewView

/**
 * Hosts CameraX [PreviewView] as a TextureView (COMPATIBLE) so Compose can
 * composite live frames. PERFORMANCE SurfaceView would draw above the whole
 * window and hide the capture controls.
 */
class AndroidPreviewContainer(context: Context) : FrameLayout(context) {
    val previewView: PreviewView = PreviewView(context).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleType = PreviewView.ScaleType.FILL_CENTER
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    init {
        setBackgroundColor(Color.BLACK)
        keepScreenOn = true
        addView(previewView)
    }
}
