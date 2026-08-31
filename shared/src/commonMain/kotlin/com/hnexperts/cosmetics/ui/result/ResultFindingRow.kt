package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.a11y_open_ingredient
import com.hnexperts.cosmetics.resources.finding_early_list
import com.hnexperts.cosmetics.resources.finding_personal_avoid
import com.hnexperts.cosmetics.resources.finding_rating_a11y
import com.hnexperts.cosmetics.resources.finding_sun_caution
import com.hnexperts.cosmetics.resources.finding_unmatched
import com.hnexperts.cosmetics.resources.finding_usage_adjusted
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultFindingRow(
    finding: Finding,
    commentSummary: String?,
    onOpen: () -> Unit
) {
    val openLabel: String = stringResource(Res.string.a11y_open_ingredient)
    Card(
        modifier = Modifier.fillMaxWidth().semantics { onClick(label = openLabel, action = null) },
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FindingPreview(
                finding = finding,
                commentSummary = commentSummary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun FindingPreview(
    finding: Finding,
    commentSummary: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = finding.ingredient.displayName, style = MaterialTheme.typography.titleLarge)
        val levelLabel: String = dangerLevelText(finding.level)
        RatingBadge(
            level = finding.level,
            label = levelLabel,
            contentDescription = stringResource(Res.string.finding_rating_a11y, levelLabel)
        )
        if (commentSummary != null) {
            Text(text = commentSummary, style = MaterialTheme.typography.bodyLarge)
        }
        FindingFlags(finding)
    }
}

@Composable
private fun FindingFlags(finding: Finding) {
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
