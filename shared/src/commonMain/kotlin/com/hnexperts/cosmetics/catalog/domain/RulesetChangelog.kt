package com.hnexperts.cosmetics.catalog.domain

/**
 * One line in the Settings ruleset history. Overlay kinds have a null
 * [versionLabel]; the packed snapshot uses the device catalog version.
 * Copy lives in composeResources — this type is not user-facing text.
 */
data class RulesetChange(
    val versionLabel: String?,
    val kind: RulesetChangeKind
)

enum class RulesetChangeKind {
    ANIMAL_DERIVED_TAGS,
    PRESET_CAUTION_TAGS,
    ALLERGEN_AND_MICROPLASTIC_TAGS,
    PACKED_SNAPSHOT
}

/**
 * Shopper-facing history of this app's catalog rules. Newest first.
 * Code-side on purpose: not a SQLite table and not a live CosIng feed.
 */
object RulesetChangelog {
    fun entries(meta: CatalogMeta?): List<RulesetChange> {
        return overlayEntries() + packedSnapshot(meta)
    }

    private fun overlayEntries(): List<RulesetChange> {
        return listOf(
            RulesetChange(versionLabel = null, kind = RulesetChangeKind.ANIMAL_DERIVED_TAGS),
            RulesetChange(versionLabel = null, kind = RulesetChangeKind.PRESET_CAUTION_TAGS),
            RulesetChange(
                versionLabel = null,
                kind = RulesetChangeKind.ALLERGEN_AND_MICROPLASTIC_TAGS
            )
        )
    }

    private fun packedSnapshot(meta: CatalogMeta?): List<RulesetChange> {
        if (meta == null) {
            return emptyList()
        }
        return listOf(
            RulesetChange(
                versionLabel = meta.catalogVersion,
                kind = RulesetChangeKind.PACKED_SNAPSHOT
            )
        )
    }
}
