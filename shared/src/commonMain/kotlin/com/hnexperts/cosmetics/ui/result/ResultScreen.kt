package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPolicy
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.finding_personal_avoid
import com.hnexperts.cosmetics.resources.finding_unmatched
import com.hnexperts.cosmetics.resources.result_disclaimer
import com.hnexperts.cosmetics.resources.result_missing
import com.hnexperts.cosmetics.resources.result_not_suitable
import com.hnexperts.cosmetics.resources.result_suitable
import com.hnexperts.cosmetics.resources.result_title
import com.hnexperts.cosmetics.resources.result_unknown_count
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.theme.RatingColors
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBack: () -> Unit
) {
    val uiState: ResultUiState by viewModel.uiState.collectAsState()
    val assessment: ProductAssessment? = uiState.assessment
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.result_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
                    }
                }
            )
        },
        bottomBar = {
            BannerAdSlot(
                visible = AdPolicy().shouldShowBanner(
                    screen = AppScreen.RESULT,
                    consentGranted = true,
                    networkAvailable = false
                )
            )
        }
    ) { padding ->
        if (assessment == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                FailureBanner(failure = uiState.failure)
                Text(text = stringResource(Res.string.result_missing))
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ResultHeader(assessment)
            }
            item {
                FailureBanner(failure = uiState.failure)
            }
            if (assessment.unknownCount > 0) {
                item {
                    Text(
                        text = pluralStringResource(
                            Res.plurals.result_unknown_count,
                            assessment.unknownCount,
                            assessment.unknownCount
                        )
                    )
                }
            }
            items(assessment.findings) { finding ->
                FindingRow(finding, viewModel)
            }
            item {
                Text(
                    text = stringResource(Res.string.result_disclaimer),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(assessment: ProductAssessment) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val title: String = assessment.productName ?: assessment.gtin ?: ""
        if (title.isNotEmpty()) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        }
        assessment.brand?.let { brand ->
            Text(text = brand, style = MaterialTheme.typography.titleMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(RatingColors.of(assessment.overall))
            )
            Text(
                text = dangerLevelText(assessment.overall),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Text(
            text = if (assessment.suitableForUser) {
                stringResource(Res.string.result_suitable)
            } else {
                stringResource(Res.string.result_not_suitable)
            }
        )
    }
}

@Composable
private fun FindingRow(finding: Finding, viewModel: ResultViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = finding.ingredient.displayName, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(RatingColors.of(finding.level))
            )
            Text(text = dangerLevelText(finding.level), style = MaterialTheme.typography.bodyMedium)
        }
        val comment = viewModel.commentFor(finding.comments)
        if (comment != null) {
            Text(text = comment.summary, style = MaterialTheme.typography.bodyMedium)
        }
        if (finding.personalAvoid) {
            Text(
                text = stringResource(Res.string.finding_personal_avoid),
                color = MaterialTheme.colorScheme.error
            )
        }
        if (finding.ingredient.id == null) {
            Text(text = stringResource(Res.string.finding_unmatched))
        }
    }
}
