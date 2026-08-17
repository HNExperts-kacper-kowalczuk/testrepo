package com.hnexperts.cosmetics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.di.AndroidAppContext
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val adsSession: AdsSession by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidAppContext.attachActivity(this)
        enableEdgeToEdge()
        setContent {
            App()
        }
        lifecycleScope.launch {
            adsSession.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidAppContext.attachActivity(this)
    }

    override fun onDestroy() {
        AndroidAppContext.detachActivity(this)
        super.onDestroy()
    }
}
