package com.hnexperts.cosmetics.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.compare_action
import com.hnexperts.cosmetics.resources.compare_select_hint
import com.hnexperts.cosmetics.resources.compare_unnamed
import com.hnexperts.cosmetics.resources.history_empty
import com.hnexperts.cosmetics.resources.history_insight_hint
import com.hnexperts.cosmetics.resources.history_insight_title
import com.hnexperts.cosmetics.resources.history_select_compare_item
import com.hnexperts.cosmetics.resources.history_title
import com.hnexperts.cosmetics.resources.shelf_empty
import com.hnexperts.cosmetics.resources.shelf_formula_changed
import com.hnexperts.cosmetics.resources.shelf_title
import com.hnexperts.cosmetics.resources.a11y_open_result
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.HistoryEntryCard
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.layout.AppLayout
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val gutter = AppLayout.horizontalGutterDp(maxWidth.value.toInt()).dp
                HistoryBody(
                    uiState = uiState,
                    viewModel = viewModel,
                    horizontalPadding = gutter
                )
            }
        }
    }
}

@Composable
private fun HistoryBody(
    uiState: HistoryUiState,
    viewModel: HistoryViewModel,
    horizontalPadding: Dp
) {
    val entries: List<HistoryEntry> = uiState.entries
    val shelf: List<ShelfItem> = uiState.shelf
    if (entries.isEmpty() && shelf.isEmpty() && uiState.failure == null) {
        Text(
            text = stringResource(Res.string.history_empty),
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp)
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            end = horizontalPadding,
            bottom = 16.dp
        )
    ) {
        item {
            val unnamedFormat: String = stringResource(Res.string.compare_unnamed)
            CompareBar(
                uiState = uiState,
                onCompare = { viewModel.compareSelected(unnamedFormat) }
            )
        }
        if (uiState.frequentConcerns.isNotEmpty()) {
            item {
                HistoryInsight(names = uiState.frequentConcerns)
            }
        }
        item {
            Text(
                text = stringResource(Res.string.shelf_title),
                modifier = Modifier.padding(vertical = 8.dp).screenHeading(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (shelf.isEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.shelf_empty),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            items(shelf, key = { item -> item.shelfKey }) { item ->
                ShelfRow(
                    item = item,
                    selected = uiState.selectedShelfKeys.contains(item.shelfKey),
                    formulaChanged = uiState.formulaChangedKeys.contains(item.shelfKey),
                    busy = uiState.busy,
                    onToggle = { viewModel.toggleShelfSelection(item) },
                    onOpen = { viewModel.reopenShelf(item) }
                )
            }
        }
        item {
            Text(
                text = stringResource(Res.string.history_title),
                modifier = Modifier.padding(vertical = 16.dp).screenHeading(),
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
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
private fun HistoryInsight(names: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.history_insight_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.screenHeading()
        )
        Text(
            text = stringResource(Res.string.history_insight_hint),
            style = MaterialTheme.typography.bodySmall
        )
        Text(text = names.joinToString(separator = ", "))
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
        CompareCheckbox(
            selected = selected,
            busy = busy,
            itemLabel = entry.name ?: entry.gtin ?: entry.inciRaw.take(40),
            onToggle = onToggle
        )
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
    formulaChanged: Boolean,
    busy: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title: String = item.name ?: item.gtin ?: item.inciRaw.take(40)
        CompareCheckbox(
            selected = selected,
            busy = busy,
            itemLabel = title,
            onToggle = onToggle
        )
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = {
                Column {
                    Text("${dangerLevelText(DangerLevelParser.parse(item.rating))} · ${item.savedAt.replace('T', ' ').take(16)}")
                    if (formulaChanged) {
                        Text(
                            text = stringResource(Res.string.shelf_formula_changed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            modifier = Modifier.clickable(
                enabled = !busy,
                role = Role.Button,
                onClickLabel = stringResource(Res.string.a11y_open_result),
                onClick = onOpen
            ).weight(1f)
        )
    }
}

@Composable
private fun CompareCheckbox(
    selected: Boolean,
    busy: Boolean,
    itemLabel: String,
    onToggle: () -> Unit
) {
    val label: String = stringResource(Res.string.history_select_compare_item, itemLabel)
    Checkbox(
        checked = selected,
        onCheckedChange = { onToggle() },
        enabled = !busy,
        modifier = Modifier.semantics { contentDescription = label }
    )
}
