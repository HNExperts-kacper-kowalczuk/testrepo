package com.hnexperts.cosmetics.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.scan_barcode_action
import com.hnexperts.cosmetics.resources.scan_barcode_label
import com.hnexperts.cosmetics.resources.scan_empty_inci
import com.hnexperts.cosmetics.resources.scan_hint_gtin
import com.hnexperts.cosmetics.resources.scan_inci_action
import com.hnexperts.cosmetics.resources.scan_inci_label
import com.hnexperts.cosmetics.resources.scan_invalid_barcode
import com.hnexperts.cosmetics.resources.scan_more_ways
import com.hnexperts.cosmetics.ui.chrome.ButtonIconLabel
import com.hnexperts.cosmetics.ui.common.UsagePicker
import com.hnexperts.cosmetics.ui.motion.Reveal
import org.jetbrains.compose.resources.stringResource

/**
 * Typing a GTIN or pasting an INCI list are fallbacks, folded away so the
 * scan-first home stays uncluttered. The section auto-expands when a scan
 * problem needs manual input (invalid barcode, unknown GTIN, empty paste).
 */
@Composable
internal fun ManualEntrySection(viewModel: ScanViewModel, uiState: ScanUiState) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    var barcode: String by remember { mutableStateOf("") }
    var inci: String by remember { mutableStateOf("") }
    var usage: ProductUsage by remember { mutableStateOf(ProductUsage.LEAVE_ON) }

    LaunchedEffect(uiState.notFoundGtin) {
        val gtin: String = uiState.notFoundGtin ?: return@LaunchedEffect
        barcode = gtin
        expanded = true
    }
    LaunchedEffect(uiState.invalidBarcode, uiState.emptyInci) {
        if (uiState.invalidBarcode || uiState.emptyInci) {
            expanded = true
        }
    }

    TextButton(onClick = { expanded = !expanded }) {
        Text(stringResource(Res.string.scan_more_ways))
    }
    Reveal(visible = expanded) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = barcode,
                onValueChange = { value -> barcode = value },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.busy,
                label = { Text(stringResource(Res.string.scan_barcode_label)) },
                placeholder = { Text(stringResource(Res.string.scan_hint_gtin)) },
                singleLine = true
            )
            Button(
                onClick = { viewModel.lookupBarcode(barcode) },
                enabled = !uiState.busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                ButtonIconLabel(
                    imageVector = Icons.Filled.Search,
                    text = stringResource(Res.string.scan_barcode_action)
                )
            }
            if (uiState.invalidBarcode) {
                Text(
                    text = stringResource(Res.string.scan_invalid_barcode),
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = inci,
                onValueChange = { value -> inci = value },
                enabled = !uiState.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                label = { Text(stringResource(Res.string.scan_inci_label)) },
                minLines = 6
            )
            UsagePicker(selected = usage, onSelect = { next -> usage = next })
            Button(
                onClick = { viewModel.evaluateTypedList(inci, usage) },
                enabled = !uiState.busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                ButtonIconLabel(
                    imageVector = Icons.Filled.DocumentScanner,
                    text = stringResource(Res.string.scan_inci_action)
                )
            }
            if (uiState.emptyInci) {
                Text(
                    text = stringResource(Res.string.scan_empty_inci),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
