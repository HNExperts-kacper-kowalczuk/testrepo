package com.hnexperts.cosmetics.ui.motion

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun rememberReduceMotion(): Boolean {
    return UIAccessibilityIsReduceMotionEnabled()
}
