package com.hnexperts.cosmetics.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.popoverPresentationController

actual fun performScanHaptic() {
    UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
}

actual fun openAppSettings() {
    val url: NSURL = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
}

actual fun openUrl(url: String) {
    val nsUrl: NSURL = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
}

actual fun sharePlainText(title: String, body: String) {
    val controller = platform.UIKit.UIActivityViewController(
        activityItems = listOf(body),
        applicationActivities = null
    )
    val root = iosRootViewController() ?: return
    controller.popoverPresentationController?.sourceView = root.view
    root.presentViewController(controller, animated = true, completion = null)
}

actual fun copyPlainText(text: String) {
    platform.UIKit.UIPasteboard.generalPasteboard.string = text
}
