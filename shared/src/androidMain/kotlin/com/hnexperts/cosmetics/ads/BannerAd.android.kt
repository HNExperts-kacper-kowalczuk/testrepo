package com.hnexperts.cosmetics.ads

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
actual fun BannerAd(
    placement: AdPlacement,
    onFailed: () -> Unit,
    modifier: Modifier
) {
    if (!AdMobConfig.isConfigured) {
        LaunchedEffect(placement) {
            onFailed()
        }
        return
    }
    val context = LocalContext.current
    val widthDp: Int = LocalConfiguration.current.screenWidthDp
    val adView: AdView = remember(placement, widthDp) {
        AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            adUnitId = AdMobConfig.bannerUnitId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }
    DisposableEffect(adView, onFailed) {
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onFailed()
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }
    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth().height(50.dp)
    )
}
