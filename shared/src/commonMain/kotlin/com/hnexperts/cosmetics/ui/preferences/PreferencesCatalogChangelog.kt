package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.RulesetChange
import com.hnexperts.cosmetics.catalog.domain.RulesetChangeKind
import com.hnexperts.cosmetics.catalog.domain.RulesetChangelog
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_changelog_allergens
import com.hnexperts.cosmetics.resources.prefs_changelog_animal
import com.hnexperts.cosmetics.resources.prefs_changelog_cautions
import com.hnexperts.cosmetics.resources.prefs_changelog_disclaimer
import com.hnexperts.cosmetics.resources.prefs_changelog_overlay_label
import com.hnexperts.cosmetics.resources.prefs_changelog_packed
import com.hnexperts.cosmetics.resources.prefs_changelog_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogChangelog(meta: CatalogMeta?) {
    Text(
        text = stringResource(Res.string.prefs_changelog_title),
        style = MaterialTheme.typography.titleSmall
    )
    RulesetChangelog.entries(meta).forEach { entry ->
        Text(
            text = changelogLine(entry),
            style = MaterialTheme.typography.bodySmall
        )
    }
    Text(
        text = stringResource(Res.string.prefs_changelog_disclaimer),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun changelogLine(entry: RulesetChange): String {
    val version: String = entry.versionLabel
        ?: stringResource(Res.string.prefs_changelog_overlay_label)
    return stringResource(summaryKey(entry.kind), version)
}

private fun summaryKey(kind: RulesetChangeKind): StringResource {
    return when (kind) {
        RulesetChangeKind.ANIMAL_DERIVED_TAGS -> Res.string.prefs_changelog_animal
        RulesetChangeKind.PRESET_CAUTION_TAGS -> Res.string.prefs_changelog_cautions
        RulesetChangeKind.ALLERGEN_AND_MICROPLASTIC_TAGS -> Res.string.prefs_changelog_allergens
        RulesetChangeKind.PACKED_SNAPSHOT -> Res.string.prefs_changelog_packed
    }
}
