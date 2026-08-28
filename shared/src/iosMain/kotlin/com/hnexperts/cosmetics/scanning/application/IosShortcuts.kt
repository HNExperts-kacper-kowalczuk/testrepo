package com.hnexperts.cosmetics.scanning.application

import org.koin.mp.KoinPlatform

fun requestScanBarcodeFromShortcut() {
    KoinPlatform.getKoin().get<LaunchIntentSession>().requestBarcodeCamera()
}
