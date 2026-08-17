package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.ads.BannerAd
import com.hnexperts.cosmetics.ads.application.AdsGate
import com.hnexperts.cosmetics.ads.application.AdsSession
import org.koin.compose.koinInject

@Composable
fun BannerAdSlot(
    screen: AppScreen,
    placement: AdPlacement,
    modifier: Modifier = Modifier
) {
    val session: AdsSession = koinInject()
    val gate: AdsGate by session.gate.collectAsState()
    if (!gate.bannerVisible(screen)) {
        return
    }
    BannerAd(
        placement = placement,
        onFailed = session::markBannerFailed,
        modifier = modifier.fillMaxWidth().height(50.dp)
    )
}
