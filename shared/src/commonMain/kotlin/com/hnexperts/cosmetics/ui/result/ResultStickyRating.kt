package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.evaluation.domain.ResultA11yCounts
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.result_a11y_summary
import com.hnexperts.cosmetics.resources.result_not_suitable
import com.hnexperts.cosmetics.resources.result_rating_a11y
import com.hnexperts.cosmetics.resources.result_suitable
import com.hnexperts.cosmetics.resources.result_usage
import com.hnexperts.cosmetics.resources.result_usage_assumed
import com.hnexperts.cosmetics.resources.usage_eye
import com.hnexperts.cosmetics.resources.usage_leave_on
import com.hnexperts.cosmetics.resources.usage_lip
import com.hnexperts.cosmetics.resources.usage_rinse_off
import com.hnexperts.cosmetics.resources.usage_spray
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.theme.RatingColors
import org.jetbrains.compose.resources.stringResource

/**
 * Traffic-light rating, usage line, and extra catalog chips. Stays on screen
 * while findings scroll. Ads stay in the result bottom bar, not here.
 */
@Composable
fun ResultStickyRating(assessment: ProductAssessment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResultHeader(assessment)
        ResultCatalogChips(assessment)
    }
}

@Composable
private fun ResultCatalogChips(assessment: ProductAssessment) {
    if (assessment.hasMicroplastics()) {
        ResultMicroplasticChip()
    }
    if (assessment.hasAnimalDerived()) {
        ResultAnimalDerivedChip()
    }
}

@Composable
private fun ResultHeader(assessment: ProductAssessment) {
    val headerColor = RatingColors.of(assessment.overall)
    val onHeader = RatingColors.onColor(assessment.overall)
    val counts: ResultA11yCounts = ResultA11yCounts.of(assessment)
    val headerSummary: String = stringResource(
        Res.string.result_a11y_summary,
        dangerLevelText(assessment.overall),
        counts.prohibited,
        counts.high,
        counts.unknown
    )
    Card(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = headerSummary
        },
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
private fun usageLabel(usage: ProductUsage): String {
    return when (usage) {
        ProductUsage.LEAVE_ON, ProductUsage.UNKNOWN -> stringResource(Res.string.usage_leave_on)
        ProductUsage.RINSE_OFF -> stringResource(Res.string.usage_rinse_off)
        ProductUsage.SPRAY -> stringResource(Res.string.usage_spray)
        ProductUsage.LIP -> stringResource(Res.string.usage_lip)
        ProductUsage.EYE -> stringResource(Res.string.usage_eye)
    }
}
