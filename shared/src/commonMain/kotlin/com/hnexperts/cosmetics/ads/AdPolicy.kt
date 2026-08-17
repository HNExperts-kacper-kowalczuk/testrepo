package com.hnexperts.cosmetics.ads

enum class AdPlacement {
    HOME,
    RESULT,
    SEARCH,
    HISTORY
}

enum class AppScreen {
    SCAN,
    CAMERA,
    OCR_REVIEW,
    RESULT,
    SEARCH,
    HISTORY,
    PREFERENCES,
    CONSENT,
    INGREDIENT_DETAIL
}

class AdPolicy {
    fun shouldShowBanner(screen: AppScreen, consentGranted: Boolean, networkAvailable: Boolean): Boolean {
        if (!consentGranted || !networkAvailable) {
            return false
        }
        return when (screen) {
            AppScreen.SCAN, AppScreen.CAMERA, AppScreen.OCR_REVIEW, AppScreen.PREFERENCES, AppScreen.CONSENT -> false
            AppScreen.RESULT, AppScreen.SEARCH, AppScreen.HISTORY, AppScreen.INGREDIENT_DETAIL -> true
        }
    }
}
