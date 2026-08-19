package com.hnexperts.cosmetics

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.di.AndroidAppContext
import com.hnexperts.cosmetics.scanning.application.LaunchIntentSession
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val adsSession: AdsSession by inject()
    private val launchIntents: LaunchIntentSession by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidAppContext.attachActivity(this)
        enableEdgeToEdge()
        setContent {
            App()
        }
        handleLaunchIntent(intent)
        lifecycleScope.launch {
            adsSession.refresh()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        AndroidAppContext.attachActivity(this)
    }

    override fun onDestroy() {
        AndroidAppContext.detachActivity(this)
        super.onDestroy()
    }

    private fun handleLaunchIntent(intent: Intent) {
        if (intent.action == ACTION_SCAN_BARCODE) {
            launchIntents.requestBarcodeCamera()
        }
    }

    companion object {
        const val ACTION_SCAN_BARCODE: String = "com.hnexperts.cosmetics.SCAN_BARCODE"
    }
}
