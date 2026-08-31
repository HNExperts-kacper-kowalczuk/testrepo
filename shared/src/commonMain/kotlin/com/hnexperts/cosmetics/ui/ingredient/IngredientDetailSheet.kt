package com.hnexperts.cosmetics.ui.ingredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.finding_early_list
import com.hnexperts.cosmetics.resources.finding_personal_avoid
import com.hnexperts.cosmetics.resources.finding_rating_a11y
import com.hnexperts.cosmetics.resources.finding_sun_caution
import com.hnexperts.cosmetics.resources.finding_unmatched
import com.hnexperts.cosmetics.resources.finding_usage_adjusted
import com.hnexperts.cosmetics.resources.ingredient_detail_also_known
import com.hnexperts.cosmetics.resources.ingredient_detail_cas
import com.hnexperts.cosmetics.resources.ingredient_detail_catalog_name
import com.hnexperts.cosmetics.resources.ingredient_detail_catalog_tags
import com.hnexperts.cosmetics.resources.ingredient_detail_functions
import com.hnexperts.cosmetics.resources.ingredient_detail_matched_alias
import com.hnexperts.cosmetics.resources.ingredient_detail_matched_exact
import com.hnexperts.cosmetics.resources.ingredient_detail_matched_fuzzy
import com.hnexperts.cosmetics.resources.ingredient_detail_position
import com.hnexperts.cosmetics.resources.ingredient_detail_restriction
import com.hnexperts.cosmetics.resources.ingredient_detail_restriction_line
import com.hnexperts.cosmetics.resources.usage_eye
import com.hnexperts.cosmetics.resources.usage_leave_on
import com.hnexperts.cosmetics.resources.usage_lip
import com.hnexperts.cosmetics.resources.usage_rinse_off
import com.hnexperts.cosmetics.resources.usage_spray
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailSheet(
    detail: IngredientDetail,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IngredientDetailBody(detail)
        }
    }
}

@Composable
private fun IngredientDetailBody(detail: IngredientDetail) {
    Text(text = detail.title, style = MaterialTheme.typography.headlineSmall)
    detail.level?.let { level ->
        val levelLabel: String = dangerLevelText(level)
        RatingBadge(
            level = level,
            label = levelLabel,
            contentDescription = stringResource(Res.string.finding_rating_a11y, levelLabel)
        )
    }
    detail.summary?.let { summary ->
        Text(text = summary, style = MaterialTheme.typography.bodyLarge)
    }
    detail.detail?.let { extra ->
        Text(text = extra, style = MaterialTheme.typography.bodyMedium)
    }
    detail.casNumbers?.let { cas ->
        Text(
            text = stringResource(Res.string.ingredient_detail_cas, cas),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    detail.catalogInciName?.let { catalogName ->
        Text(
            text = stringResource(Res.string.ingredient_detail_catalog_name, catalogName),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (detail.aliases.isNotEmpty()) {
        LabeledBlock(title = stringResource(Res.string.ingredient_detail_also_known)) {
            Text(
                text = detail.aliases.joinToString(separator = ", "),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    TagChipRow(
        title = stringResource(Res.string.ingredient_detail_functions),
        tags = detail.functionTags,
        labelOf = IngredientDetailTagCopy::functionLabel
    )
    TagChipRow(
        title = stringResource(Res.string.ingredient_detail_catalog_tags),
        tags = detail.regulatoryTags,
        labelOf = IngredientDetailTagCopy::regulatoryLabel
    )
    RestrictionBlock(restriction = detail.restriction)
    matchMethodText(detail.matchMethod)?.let { resource ->
        Text(text = stringResource(resource), style = MaterialTheme.typography.bodySmall)
    }
    detail.listPosition?.let { position ->
        Text(
            text = stringResource(Res.string.ingredient_detail_position, position),
            style = MaterialTheme.typography.bodySmall
        )
    }
    CautionLines(detail)
}

@Composable
private fun RestrictionBlock(restriction: UsageRestriction?) {
    if (restriction == null) {
        return
    }
    val rows: List<Pair<StringResource, DangerLevel>> = restrictionRows(restriction)
    if (rows.isEmpty()) {
        return
    }
    LabeledBlock(title = stringResource(Res.string.ingredient_detail_restriction)) {
        rows.forEach { row ->
            val usageLabel: String = stringResource(row.first)
            val levelLabel: String = dangerLevelText(row.second)
            Text(
                text = stringResource(Res.string.ingredient_detail_restriction_line, usageLabel, levelLabel),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CautionLines(detail: IngredientDetail) {
    if (detail.sunCaution) {
        Text(text = stringResource(Res.string.finding_sun_caution), style = MaterialTheme.typography.bodySmall)
    }
    if (detail.earlyList) {
        Text(text = stringResource(Res.string.finding_early_list), style = MaterialTheme.typography.bodySmall)
    }
    if (detail.usageAdjusted) {
        Text(text = stringResource(Res.string.finding_usage_adjusted), style = MaterialTheme.typography.bodySmall)
    }
    if (detail.personalAvoid) {
        Text(
            text = stringResource(Res.string.finding_personal_avoid),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    if (detail.unmatched) {
        Text(text = stringResource(Res.string.finding_unmatched))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChipRow(
    title: String,
    tags: List<String>,
    labelOf: (String) -> StringResource?
) {
    val labels: List<String> = tags.mapNotNull { tag ->
        val resource: StringResource = labelOf(tag) ?: return@mapNotNull null
        stringResource(resource)
    }
    if (labels.isEmpty()) {
        return
    }
    LabeledBlock(title = title) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEach { label ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledBlock(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

private fun restrictionRows(restriction: UsageRestriction): List<Pair<StringResource, DangerLevel>> {
    return listOfNotNull(
        levelPair(Res.string.usage_leave_on, restriction.leaveOn),
        levelPair(Res.string.usage_rinse_off, restriction.rinseOff),
        levelPair(Res.string.usage_lip, restriction.lip),
        levelPair(Res.string.usage_eye, restriction.eye),
        levelPair(Res.string.usage_spray, restriction.spray)
    )
}

private fun levelPair(label: StringResource, raw: String?): Pair<StringResource, DangerLevel>? {
    val level: DangerLevel = parseLevel(raw) ?: return null
    return label to level
}

private fun parseLevel(raw: String?): DangerLevel? {
    if (raw.isNullOrBlank()) {
        return null
    }
    return DangerLevel.entries.firstOrNull { level -> level.name.equals(raw, ignoreCase = true) }
}

private fun matchMethodText(method: MatchMethod?): StringResource? {
    return when (method) {
        MatchMethod.EXACT -> Res.string.ingredient_detail_matched_exact
        MatchMethod.ALIAS -> Res.string.ingredient_detail_matched_alias
        MatchMethod.FUZZY -> Res.string.ingredient_detail_matched_fuzzy
        MatchMethod.UNMATCHED, null -> null
    }
}
