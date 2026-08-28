package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ChoiceChip<T>(
    val value: T,
    val label: String,
    val selected: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChipFlow(
    chips: List<ChoiceChip<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.selected,
                onClick = { onSelect(chip.value) },
                label = { Text(chip.label) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}
