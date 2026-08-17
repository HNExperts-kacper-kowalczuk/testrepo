package com.hnexperts.cosmetics.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPolicy
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.history_empty
import com.hnexperts.cosmetics.resources.history_title
import com.hnexperts.cosmetics.scanning.data.HistoryEntry
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenResult: () -> Unit
) {
    val uiState: HistoryUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onOpenResult()
            viewModel.consumeNavigation()
        }
    }
    val entries: List<HistoryEntry> = uiState.entries
    Scaffold(
        bottomBar = {
            BannerAdSlot(
                visible = AdPolicy().shouldShowBanner(
                    AppScreen.HISTORY,
                    consentGranted = true,
                    networkAvailable = false
                )
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Text(
                text = stringResource(Res.string.history_empty),
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Text(
                    text = stringResource(Res.string.history_title),
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )
            }
            items(entries, key = { entry -> entry.id }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.rating) },
                    supportingContent = { Text(entry.scannedAt) },
                    overlineContent = { Text(entry.source) },
                    modifier = Modifier.clickable(enabled = !uiState.busy) {
                        viewModel.reopen(entry)
                    }
                )
            }
        }
    }
}
