package com.hnexperts.cosmetics.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyleMedium

actual fun performScanHaptic() {
    UIImpactFeedbackGenerator(style = UIImpactFeedbackStyleMedium).impactOccurred()
}

actual fun openAppSettings() {
    val url: NSURL = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
}
