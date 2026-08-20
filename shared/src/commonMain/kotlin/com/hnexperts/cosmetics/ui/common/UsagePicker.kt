package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.usage_eye
import com.hnexperts.cosmetics.resources.usage_leave_on
import com.hnexperts.cosmetics.resources.usage_lip
import com.hnexperts.cosmetics.resources.usage_prompt
import com.hnexperts.cosmetics.resources.usage_rinse_off
import com.hnexperts.cosmetics.resources.usage_spray
import org.jetbrains.compose.resources.stringResource

@Composable
fun UsagePicker(
    selected: ProductUsage,
    onSelect: (ProductUsage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(Res.string.usage_prompt), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            UsageChip(ProductUsage.LEAVE_ON, selected, onSelect, stringResource(Res.string.usage_leave_on))
            UsageChip(ProductUsage.RINSE_OFF, selected, onSelect, stringResource(Res.string.usage_rinse_off))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            UsageChip(ProductUsage.SPRAY, selected, onSelect, stringResource(Res.string.usage_spray))
            UsageChip(ProductUsage.LIP, selected, onSelect, stringResource(Res.string.usage_lip))
            UsageChip(ProductUsage.EYE, selected, onSelect, stringResource(Res.string.usage_eye))
        }
    }
}

@Composable
fun usageWord(usage: ProductUsage): String {
    return when (usage) {
        ProductUsage.LEAVE_ON, ProductUsage.UNKNOWN -> stringResource(Res.string.usage_leave_on)
        ProductUsage.RINSE_OFF -> stringResource(Res.string.usage_rinse_off)
        ProductUsage.SPRAY -> stringResource(Res.string.usage_spray)
        ProductUsage.LIP -> stringResource(Res.string.usage_lip)
        ProductUsage.EYE -> stringResource(Res.string.usage_eye)
    }
}

@Composable
private fun UsageChip(
    usage: ProductUsage,
    selected: ProductUsage,
    onSelect: (ProductUsage) -> Unit,
    label: String
) {
    FilterChip(
        selected = selected == usage,
        onClick = { onSelect(usage) },
        label = { Text(label) }
    )
}
