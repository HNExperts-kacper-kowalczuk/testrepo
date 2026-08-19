package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.finding_personal_avoid
import com.hnexperts.cosmetics.resources.finding_rating_a11y
import com.hnexperts.cosmetics.resources.finding_unmatched
import com.hnexperts.cosmetics.resources.finding_usage_adjusted
import com.hnexperts.cosmetics.resources.result_disclaimer
import com.hnexperts.cosmetics.resources.result_missing
import com.hnexperts.cosmetics.resources.result_not_suitable
import com.hnexperts.cosmetics.resources.result_rating_a11y
import com.hnexperts.cosmetics.resources.result_suitable
import com.hnexperts.cosmetics.resources.result_title
import com.hnexperts.cosmetics.resources.result_unknown_count
import com.hnexperts.cosmetics.resources.result_usage
import com.hnexperts.cosmetics.resources.result_usage_assumed
import com.hnexperts.cosmetics.resources.usage_eye
import com.hnexperts.cosmetics.resources.usage_leave_on
import com.hnexperts.cosmetics.resources.usage_lip
import com.hnexperts.cosmetics.resources.usage_rinse_off
import com.hnexperts.cosmetics.resources.usage_spray
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.RatingBadge
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
            BannerAdSlot(screen = AppScreen.RESULT, placement = AdPlacement.RESULT)
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
    val headerColor = RatingColors.of(assessment.overall)
    val onHeader = RatingColors.onColor(assessment.overall)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = headerColor, contentColor = onHeader)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val title: String = assessment.productName ?: assessment.gtin ?: ""
            if (title.isNotEmpty()) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
            }
            assessment.brand?.let { brand ->
                Text(text = brand, style = MaterialTheme.typography.titleMedium)
            }
            RatingChip(assessment)
            Text(
                text = if (assessment.suitableForUser) {
                    stringResource(Res.string.result_suitable)
                } else {
                    stringResource(Res.string.result_not_suitable)
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(Res.string.result_usage, usageLabel(assessment.usage)),
                style = MaterialTheme.typography.bodyMedium
            )
            if (assessment.usageAssumed) {
                Text(
                    text = stringResource(Res.string.result_usage_assumed),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * The shape+word rating mark sits on a white chip so its colour-independent
 * shape stays visible on the tinted header.
 */
@Composable
private fun RatingChip(assessment: ProductAssessment) {
    val overallLabel: String = dangerLevelText(assessment.overall)
    val overallDescription: String = stringResource(Res.string.result_rating_a11y, overallLabel)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        RatingBadge(
            level = assessment.overall,
            label = overallLabel,
            contentDescription = overallDescription,
            large = true,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun FindingRow(finding: Finding, viewModel: ResultViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = finding.ingredient.displayName, style = MaterialTheme.typography.titleLarge)
            val levelLabel: String = dangerLevelText(finding.level)
            val levelDescription: String = stringResource(Res.string.finding_rating_a11y, levelLabel)
            RatingBadge(
                level = finding.level,
                label = levelLabel,
                contentDescription = levelDescription
            )
            val comment = viewModel.commentFor(finding.comments)
            if (comment != null) {
                Text(text = comment.summary, style = MaterialTheme.typography.bodyLarge)
            }
            if (finding.usageAdjusted) {
                Text(
                    text = stringResource(Res.string.finding_usage_adjusted),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (finding.personalAvoid) {
                Text(
                    text = stringResource(Res.string.finding_personal_avoid),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (finding.ingredient.id == null) {
                Text(text = stringResource(Res.string.finding_unmatched))
            }
        }
    }
}

@Composable
private fun usageLabel(usage: ProductUsage): String {
    return when (usage) {
        ProductUsage.LEAVE_ON, ProductUsage.UNKNOWN -> stringResource(Res.string.usage_leave_on)
        ProductUsage.RINSE_OFF -> stringResource(Res.string.usage_rinse_off)
        ProductUsage.SPRAY -> stringResource(Res.string.usage_spray)
        ProductUsage.LIP -> stringResource(Res.string.usage_lip)
        ProductUsage.EYE -> stringResource(Res.string.usage_eye)
    }
}
