package com.hnexperts.cosmetics.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
actual fun BannerAd(
    placement: AdPlacement,
    onFailed: () -> Unit,
    modifier: Modifier
) {
    LaunchedEffect(placement) {
        onFailed()
    }
}
