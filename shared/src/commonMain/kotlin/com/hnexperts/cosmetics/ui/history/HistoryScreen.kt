package com.hnexperts.cosmetics.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.compare_action
import com.hnexperts.cosmetics.resources.compare_select_hint
import com.hnexperts.cosmetics.resources.history_empty
import com.hnexperts.cosmetics.resources.history_title
import com.hnexperts.cosmetics.resources.shelf_empty
import com.hnexperts.cosmetics.resources.shelf_title
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.HistoryEntryCard
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenResult: () -> Unit,
    onOpenCompare: () -> Unit
) {
    val uiState: HistoryUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    LaunchedEffect(uiState.navigateToResult, uiState.navigateToCompare) {
        if (uiState.navigateToResult) {
            onOpenResult()
            viewModel.consumeNavigation()
        } else if (uiState.navigateToCompare) {
            onOpenCompare()
            viewModel.consumeNavigation()
        }
    }
    Scaffold(
        bottomBar = {
            BannerAdSlot(screen = AppScreen.HISTORY, placement = AdPlacement.HISTORY)
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FailureBanner(
                failure = uiState.failure,
                onRetry = { viewModel.refresh() }
            )
            HistoryBody(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun HistoryBody(uiState: HistoryUiState, viewModel: HistoryViewModel) {
    val entries: List<HistoryEntry> = uiState.entries
    val shelf: List<ShelfItem> = uiState.shelf
    if (entries.isEmpty() && shelf.isEmpty() && uiState.failure == null) {
        Text(
            text = stringResource(Res.string.history_empty),
            modifier = Modifier.padding(16.dp)
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CompareBar(uiState = uiState, onCompare = viewModel::compareSelected)
        }
        item {
            Text(
                text = stringResource(Res.string.shelf_title),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (shelf.isEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.shelf_empty),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            items(shelf, key = { item -> item.shelfKey }) { item ->
                ShelfRow(
                    item = item,
                    selected = uiState.selectedShelfKeys.contains(item.shelfKey),
                    busy = uiState.busy,
                    onToggle = { viewModel.toggleShelfSelection(item) },
                    onOpen = { viewModel.reopenShelf(item) }
                )
            }
        }
        item {
            Text(
                text = stringResource(Res.string.history_title),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        items(entries, key = { entry -> entry.id }) { entry ->
            HistorySelectableRow(
                entry = entry,
                selected = uiState.selectedHistoryIds.contains(entry.id),
                busy = uiState.busy,
                onToggle = { viewModel.toggleHistorySelection(entry) },
                onOpen = { viewModel.reopen(entry) }
            )
        }
    }
}

@Composable
private fun CompareBar(uiState: HistoryUiState, onCompare: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.compare_select_hint),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = onCompare,
            enabled = uiState.canCompare && !uiState.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.compare_action))
        }
    }
}

@Composable
private fun HistorySelectableRow(
    entry: HistoryEntry,
    selected: Boolean,
    busy: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() }, enabled = !busy)
        HistoryEntryCard(
            entry = entry,
            enabled = !busy,
            onOpen = { onOpen() },
            modifier = Modifier.padding(vertical = 4.dp).weight(1f)
        )
    }
}

@Composable
private fun ShelfRow(
    item: ShelfItem,
    selected: Boolean,
    busy: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() }, enabled = !busy)
        val title: String = item.name ?: item.gtin ?: item.inciRaw.take(40)
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = {
                Text("${dangerLevelText(DangerLevelParser.parse(item.rating))} · ${item.savedAt.replace('T', ' ').take(16)}")
            },
            modifier = Modifier.clickable(enabled = !busy, onClick = onOpen).weight(1f)
        )
    }
}
