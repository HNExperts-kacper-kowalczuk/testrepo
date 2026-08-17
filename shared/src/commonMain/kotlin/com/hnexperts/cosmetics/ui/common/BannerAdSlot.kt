package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.ad_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun BannerAdSlot(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) {
        return
    }
    Surface(
        modifier = modifier.fillMaxWidth().height(50.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(
                text = stringResource(Res.string.ad_placeholder),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
