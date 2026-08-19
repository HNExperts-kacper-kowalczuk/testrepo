package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.source_barcode
import com.hnexperts.cosmetics.resources.source_manual
import com.hnexperts.cosmetics.resources.source_ocr
import com.hnexperts.cosmetics.resources.source_online
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.ui.theme.RatingColors
import org.jetbrains.compose.resources.stringResource

/**
 * One past evaluation: a colour strip for the rating, the rating word,
 * and where the list came from. Shared by the Scan home and History.
 */
@Composable
fun HistoryEntryCard(
    entry: HistoryEntry,
    enabled: Boolean,
    onOpen: (HistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val level: DangerLevel = DangerLevelParser.parse(entry.rating)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onOpen(entry) }
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(RatingColors.of(level))
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val title: String? = entry.name ?: entry.gtin
                if (!title.isNullOrBlank()) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Text(text = dangerLevelText(level), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${sourceLabel(entry.source)} · ${prettyTimestamp(entry.scannedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun sourceLabel(source: String): String {
    return when (source) {
        "barcode" -> stringResource(Res.string.source_barcode)
        "ocr" -> stringResource(Res.string.source_ocr)
        "online" -> stringResource(Res.string.source_online)
        else -> stringResource(Res.string.source_manual)
    }
}

private fun prettyTimestamp(scannedAt: String): String {
    return scannedAt.replace('T', ' ').take(16)
}
