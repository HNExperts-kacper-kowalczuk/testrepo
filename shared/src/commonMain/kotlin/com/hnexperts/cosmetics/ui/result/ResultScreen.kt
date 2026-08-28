package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.application.ShareCopy
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.finding_personal_avoid
import com.hnexperts.cosmetics.resources.finding_early_list
import com.hnexperts.cosmetics.resources.finding_rating_a11y
import com.hnexperts.cosmetics.resources.finding_sun_caution
import com.hnexperts.cosmetics.resources.finding_unmatched
import com.hnexperts.cosmetics.resources.finding_usage_adjusted
import com.hnexperts.cosmetics.resources.result_alternatives
import com.hnexperts.cosmetics.resources.result_alternatives_source
import com.hnexperts.cosmetics.resources.result_check_label
import com.hnexperts.cosmetics.resources.result_disclaimer
import com.hnexperts.cosmetics.resources.result_missing
import com.hnexperts.cosmetics.resources.result_not_suitable
import com.hnexperts.cosmetics.resources.result_pack_verified
import com.hnexperts.cosmetics.resources.result_shelf_add
import com.hnexperts.cosmetics.resources.result_shelf_remove
import com.hnexperts.cosmetics.resources.result_suitable
import com.hnexperts.cosmetics.resources.result_title
import com.hnexperts.cosmetics.resources.result_unknown_count
import com.hnexperts.cosmetics.resources.result_usage_pick
import com.hnexperts.cosmetics.resources.share_scanned_at
import com.hnexperts.cosmetics.resources.share_scanned_product
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.UsagePicker
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.layout.AppLayout
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBack: () -> Unit,
    onCheckLabel: () -> Unit
) {
    val uiState: ResultUiState by viewModel.uiState.collectAsState()
    val assessment: ProductAssessment? = uiState.assessment
    LaunchedEffect(uiState.navigateToCamera) {
        if (uiState.navigateToCamera) {
            onCheckLabel()
            viewModel.consumeNavigation()
        }
    }
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
        ResultAssessmentBody(
            assessment = assessment,
            uiState = uiState,
            viewModel = viewModel,
            padding = padding
        )
    }
}

@Composable
private fun ResultAssessmentBody(
    assessment: ProductAssessment,
    uiState: ResultUiState,
    viewModel: ResultViewModel,
    padding: PaddingValues
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
        val gutter = AppLayout.horizontalGutterDp(maxWidth.value.toInt()).dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = gutter, end = gutter, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stickyHeader(key = "result-rating") {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultStickyRating(assessment)
                }
            }
            resultActions(assessment, uiState, viewModel)
            resultDetails(assessment, uiState, viewModel)
        }
    }
}

private fun LazyListScope.resultActions(
    assessment: ProductAssessment,
    uiState: ResultUiState,
    viewModel: ResultViewModel
) {
    if (assessment.usageAssumed) {
        item {
            ResultUsageConfirm(onSelect = viewModel::setUsage)
        }
    }
    if (showCategoryPicker(assessment, uiState)) {
        item {
            ResultCategoryPicker(
                choices = uiState.categoryChoices,
                onPick = viewModel::setCategory,
                onSkip = viewModel::skipCategory
            )
        }
    }
    if (assessment.packVerified) {
        item {
            Text(text = stringResource(Res.string.result_pack_verified))
        }
    }
    item {
        Button(
            onClick = viewModel::checkTheLabel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.result_check_label))
        }
    }
    item {
        Button(
            onClick = viewModel::toggleShelf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.onShelf) {
                    stringResource(Res.string.result_shelf_remove)
                } else {
                    stringResource(Res.string.result_shelf_add)
                }
            )
        }
    }
    item {
        ResultShareActions(
            copy = shareCopyFor(assessment),
            viewModel = viewModel
        )
    }
}

@Composable
private fun shareCopyFor(assessment: ProductAssessment): ShareCopy {
    return ShareCopy(
        scannedProduct = stringResource(Res.string.share_scanned_product),
        suitable = stringResource(Res.string.result_suitable),
        notSuitable = stringResource(Res.string.result_not_suitable),
        disclaimer = stringResource(Res.string.result_disclaimer),
        overallLabel = dangerLevelText(assessment.overall),
        scannedAtLabel = stringResource(Res.string.share_scanned_at)
    )
}

private fun LazyListScope.resultDetails(
    assessment: ProductAssessment,
    uiState: ResultUiState,
    viewModel: ResultViewModel
) {
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
    if (uiState.alternatives.isNotEmpty()) {
        resultAlternatives(uiState, viewModel)
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

private fun LazyListScope.resultAlternatives(
    uiState: ResultUiState,
    viewModel: ResultViewModel
) {
    item {
        Text(
            text = stringResource(Res.string.result_alternatives),
            style = MaterialTheme.typography.titleMedium
        )
    }
    item {
        Text(
            text = stringResource(Res.string.result_alternatives_source),
            style = MaterialTheme.typography.bodySmall
        )
    }
    items(uiState.alternatives, key = { alternative -> alternative.product.id }) { alternative ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.openAlternative(alternative) }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = alternative.product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = dangerLevelText(alternative.assessment.overall))
            }
        }
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
            if (finding.sunCaution()) {
                Text(
                    text = stringResource(Res.string.finding_sun_caution),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (finding.earlyListConcern()) {
                Text(
                    text = stringResource(Res.string.finding_early_list),
                    style = MaterialTheme.typography.bodySmall
                )
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
private fun ResultUsageConfirm(onSelect: (ProductUsage) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.result_usage_pick),
            style = MaterialTheme.typography.bodyMedium
        )
        UsagePicker(selected = ProductUsage.UNKNOWN, onSelect = onSelect)
    }
}

private fun showCategoryPicker(assessment: ProductAssessment, uiState: ResultUiState): Boolean {
    if (!assessment.category.isNullOrBlank()) {
        return false
    }
    if (uiState.categorySkipped) {
        return false
    }
    return uiState.categoryChoices.isNotEmpty()
}
