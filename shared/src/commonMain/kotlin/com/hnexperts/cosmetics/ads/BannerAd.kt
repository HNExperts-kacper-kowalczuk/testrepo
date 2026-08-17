package com.hnexperts.cosmetics.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BannerAd(
    placement: AdPlacement,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier
)
