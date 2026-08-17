package com.hnexperts.cosmetics.platform

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.hnexperts.cosmetics.di.AndroidAppContext

actual fun performScanHaptic() {
    val context = AndroidAppContext.current() ?: return
    val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager: VibratorManager = context.getSystemService(VibratorManager::class.java)
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(40)
    }
}

actual fun openAppSettings() {
    val context = AndroidAppContext.current() ?: return
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
