package com.hnexperts.cosmetics.platform

import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

fun iosRootViewController(): UIViewController? {
    return iosKeyWindow()?.rootViewController
}

fun iosKeyWindow(): UIWindow? {
    val scenes = UIApplication.sharedApplication.connectedScenes
    val windowScenes: List<UIWindowScene> = scenes.mapNotNull { scene -> scene as? UIWindowScene }
    val foreground: UIWindowScene = windowScenes.firstOrNull { scene ->
        scene.activationState == UISceneActivationStateForegroundActive
    } ?: windowScenes.firstOrNull() ?: return null
    val key: UIWindow? = foreground.windows.firstOrNull { candidate ->
        (candidate as? UIWindow)?.isKeyWindow() == true
    } as? UIWindow
    if (key != null) {
        return key
    }
    return foreground.windows.firstOrNull() as? UIWindow
}
