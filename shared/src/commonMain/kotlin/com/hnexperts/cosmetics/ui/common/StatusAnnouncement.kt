package com.hnexperts.cosmetics.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

@Composable
fun StatusAnnouncement(message: String?, modifier: Modifier = Modifier) {
    if (message == null) {
        return
    }
    Text(
        text = message,
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
        }
    )
}
