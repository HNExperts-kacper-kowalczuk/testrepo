package com.hnexperts.cosmetics.ui.confirm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.a11y_pick_ingredient
import com.hnexperts.cosmetics.resources.confirm_pick_ingredient
import com.hnexperts.cosmetics.resources.confirm_suggestion_nearby
import com.hnexperts.cosmetics.resources.confirm_suggestion_search
import com.hnexperts.cosmetics.resources.confirm_suggestions_empty
import com.hnexperts.cosmetics.resources.confirm_suggestions_working
import com.hnexperts.cosmetics.resources.search_placeholder_ingredients
import com.hnexperts.cosmetics.scanning.domain.IngredientSuggestion
import com.hnexperts.cosmetics.ui.common.BusyStatus
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmIngredientPickerSheet(
    picker: ConfirmPickerState,
    onQueryChange: (String) -> Unit,
    onPick: (IngredientSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.confirm_pick_ingredient),
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                value = picker.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.search_placeholder_ingredients)) },
                singleLine = true
            )
            PickerResults(picker = picker, onPick = onPick)
        }
    }
}

@Composable
private fun PickerResults(
    picker: ConfirmPickerState,
    onPick: (IngredientSuggestion) -> Unit
) {
    if (picker.busy && picker.nearby.isEmpty() && picker.search.isEmpty()) {
        BusyStatus(message = stringResource(Res.string.confirm_suggestions_working))
        return
    }
    if (picker.nearby.isEmpty() && picker.search.isEmpty()) {
        Text(
            text = stringResource(Res.string.confirm_suggestions_empty),
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (picker.nearby.isNotEmpty()) {
            item(key = "nearby-header") {
                Text(
                    text = stringResource(Res.string.confirm_suggestion_nearby),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(picker.nearby, key = { suggestion -> "nearby-${suggestion.id}" }) { suggestion ->
                SuggestionRow(suggestion = suggestion, onPick = onPick)
            }
        }
        if (picker.search.isNotEmpty()) {
            item(key = "search-header") {
                Text(
                    text = stringResource(Res.string.confirm_suggestion_search),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(picker.search, key = { suggestion -> "search-${suggestion.id}" }) { suggestion ->
                SuggestionRow(suggestion = suggestion, onPick = onPick)
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: IngredientSuggestion,
    onPick: (IngredientSuggestion) -> Unit
) {
    val pickLabel: String = stringResource(Res.string.a11y_pick_ingredient, suggestion.inciName)
    ListItem(
        headlineContent = { Text(suggestion.inciName) },
        modifier = Modifier.clickable(
            role = Role.Button,
            onClickLabel = pickLabel
        ) { onPick(suggestion) }
    )
}
