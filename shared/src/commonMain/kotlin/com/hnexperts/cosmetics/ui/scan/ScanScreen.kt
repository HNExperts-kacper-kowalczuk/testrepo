package com.hnexperts.cosmetics.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.scan_barcode_action
import com.hnexperts.cosmetics.resources.scan_barcode_label
import com.hnexperts.cosmetics.resources.scan_camera_note
import com.hnexperts.cosmetics.resources.scan_empty_inci
import com.hnexperts.cosmetics.resources.scan_hint_gtin
import com.hnexperts.cosmetics.resources.scan_inci_action
import com.hnexperts.cosmetics.resources.scan_inci_label
import com.hnexperts.cosmetics.resources.scan_invalid_barcode
import com.hnexperts.cosmetics.resources.scan_not_found_body
import com.hnexperts.cosmetics.resources.scan_not_found_title
import com.hnexperts.cosmetics.resources.scan_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onResult: () -> Unit
) {
    var barcode: String by remember { mutableStateOf("") }
    var inci: String by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }
    var notFound: String? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(Res.string.scan_title), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(Res.string.scan_camera_note), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = barcode,
            onValueChange = { value -> barcode = value },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.scan_barcode_label)) },
            placeholder = { Text(stringResource(Res.string.scan_hint_gtin)) },
            singleLine = true
        )
        Button(
            onClick = {
                when (val lookup = viewModel.lookupBarcode(barcode)) {
                    is BarcodeLookup.Found -> {
                        error = null
                        notFound = null
                        onResult()
                    }
                    is BarcodeLookup.NotFound -> {
                        error = null
                        notFound = lookup.gtin
                    }
                    is BarcodeLookup.Invalid -> {
                        notFound = null
                        error = "invalid"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.scan_barcode_action))
        }
        if (error == "invalid") {
            Text(
                text = stringResource(Res.string.scan_invalid_barcode),
                color = MaterialTheme.colorScheme.error
            )
        }
        if (notFound != null) {
            Text(text = stringResource(Res.string.scan_not_found_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(Res.string.scan_not_found_body))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = inci,
            onValueChange = { value -> inci = value },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            label = { Text(stringResource(Res.string.scan_inci_label)) }
        )
        Button(
            onClick = {
                if (inci.isBlank()) {
                    error = "empty-inci"
                } else {
                    error = null
                    viewModel.evaluateTypedList(inci)
                    onResult()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.scan_inci_action))
        }
        if (error == "empty-inci") {
            Text(
                text = stringResource(Res.string.scan_empty_inci),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
