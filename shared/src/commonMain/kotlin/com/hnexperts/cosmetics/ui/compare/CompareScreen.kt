package com.hnexperts.cosmetics.ui.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.evaluation.application.ComparedProduct
import com.hnexperts.cosmetics.evaluation.application.CompareSummary
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.compare_empty
import com.hnexperts.cosmetics.resources.compare_shared_avoids
import com.hnexperts.cosmetics.resources.compare_title
import com.hnexperts.cosmetics.resources.compare_unique_concerns
import com.hnexperts.cosmetics.resources.result_not_suitable
import com.hnexperts.cosmetics.resources.result_suitable
import com.hnexperts.cosmetics.resources.result_usage
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.common.usageWord
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onBack: () -> Unit
) {
    val uiState: CompareUiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.compare_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FailureBanner(failure = uiState.failure)
            val summary: CompareSummary? = uiState.summary
            if (summary == null) {
                Text(text = stringResource(Res.string.compare_empty))
            } else {
                summary.products.forEach { product ->
                    ComparedProductCard(
                        product = product,
                        uniqueNames = summary.uniqueHighOrProhibited[product.id].orEmpty()
                    )
                }
                if (summary.sharedPersonalAvoids.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.compare_shared_avoids),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = summary.sharedPersonalAvoids.joinToString(separator = ", "))
                }
            }
        }
    }
}

@Composable
private fun ComparedProductCard(
    product: ComparedProduct,
    uniqueNames: List<String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = product.label, style = MaterialTheme.typography.titleLarge)
            RatingBadge(
                level = product.assessment.overall,
                label = dangerLevelText(product.assessment.overall),
                contentDescription = dangerLevelText(product.assessment.overall)
            )
            Text(
                text = if (product.assessment.suitableForUser) {
                    stringResource(Res.string.result_suitable)
                } else {
                    stringResource(Res.string.result_not_suitable)
                }
            )
            Text(
                text = stringResource(Res.string.result_usage, usageWord(product.assessment.usage)),
                style = MaterialTheme.typography.bodyMedium
            )
            if (uniqueNames.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.compare_unique_concerns),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = uniqueNames.joinToString(separator = ", "))
            }
        }
    }
}
