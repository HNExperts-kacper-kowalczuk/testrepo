package com.hnexperts.cosmetics.ui.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.confirm_add
import com.hnexperts.cosmetics.resources.confirm_empty
import com.hnexperts.cosmetics.resources.confirm_evaluate
import com.hnexperts.cosmetics.resources.confirm_fuzzy_accept
import com.hnexperts.cosmetics.resources.confirm_fuzzy_prompt
import com.hnexperts.cosmetics.resources.confirm_fuzzy_reject
import com.hnexperts.cosmetics.resources.confirm_pending_fuzzy
import com.hnexperts.cosmetics.resources.confirm_raw_label
import com.hnexperts.cosmetics.resources.confirm_remove
import com.hnexperts.cosmetics.resources.confirm_title
import com.hnexperts.cosmetics.resources.confirm_unknown
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.ReviewToken
import com.hnexperts.cosmetics.ui.common.FailureBanner
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmIngredientsScreen(
    viewModel: ConfirmIngredientsViewModel,
    onBack: () -> Unit,
    onResult: () -> Unit
) {
    val uiState: ConfirmUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onResult()
            viewModel.consumeNavigation()
        }
    }
    val draft = uiState.draft
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.confirm_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FailureBanner(failure = uiState.failure)
            if (draft == null) {
                Text(text = stringResource(Res.string.confirm_empty))
                return@Column
            }
            if (draft.hasPendingFuzzy()) {
                Text(
                    text = stringResource(Res.string.confirm_pending_fuzzy),
                    color = MaterialTheme.colorScheme.error
                )
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(draft.tokens, key = { token -> token.key }) { token ->
                    TokenEditor(
                        token = token,
                        enabled = !uiState.busy,
                        onRawChange = { text -> viewModel.updateRaw(token.key, text) },
                        onAcceptFuzzy = { viewModel.acceptFuzzy(token.key) },
                        onRejectFuzzy = { viewModel.rejectFuzzy(token.key) },
                        onRemove = { viewModel.removeToken(token.key) }
                    )
                }
            }
            TextButton(onClick = viewModel::addToken, enabled = !uiState.busy) {
                Text(stringResource(Res.string.confirm_add))
            }
            Button(
                onClick = viewModel::evaluate,
                enabled = !uiState.busy && !draft.hasPendingFuzzy(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.confirm_evaluate))
            }
        }
    }
}

@Composable
private fun TokenEditor(
    token: ReviewToken,
    enabled: Boolean,
    onRawChange: (String) -> Unit,
    onAcceptFuzzy: () -> Unit,
    onRejectFuzzy: () -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = token.rawText,
            onValueChange = onRawChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.confirm_raw_label)) }
        )
        if (token.matchMethod == MatchMethod.UNMATCHED) {
            Text(text = stringResource(Res.string.confirm_unknown), style = MaterialTheme.typography.bodySmall)
        }
        if (token.fuzzyDecision == FuzzyDecision.PENDING) {
            Text(
                text = stringResource(Res.string.confirm_fuzzy_prompt, token.rawText, token.suggestedName),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAcceptFuzzy, enabled = enabled) {
                    Text(stringResource(Res.string.confirm_fuzzy_accept))
                }
                TextButton(onClick = onRejectFuzzy, enabled = enabled) {
                    Text(stringResource(Res.string.confirm_fuzzy_reject))
                }
            }
        }
        TextButton(onClick = onRemove, enabled = enabled) {
            Text(stringResource(Res.string.confirm_remove))
        }
    }
}
