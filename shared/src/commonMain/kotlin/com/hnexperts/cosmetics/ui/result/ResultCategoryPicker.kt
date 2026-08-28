package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.result_category_pick
import com.hnexperts.cosmetics.resources.result_category_skip
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultCategoryPicker(
    choices: List<String>,
    onPick: (String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.result_category_pick),
            style = MaterialTheme.typography.bodyMedium
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { category ->
                FilterChip(
                    selected = false,
                    onClick = { onPick(category) },
                    label = { Text(category) }
                )
            }
        }
        TextButton(onClick = onSkip) {
            Text(stringResource(Res.string.result_category_skip))
        }
    }
}
