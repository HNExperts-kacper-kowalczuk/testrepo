package com.hnexperts.cosmetics.catalog.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RulesetChangelogTest {
    @Test
    fun overlayHistoryComesBeforePackedSnapshot() {
        val kinds: List<RulesetChangeKind> = RulesetChangelog.entries(CatalogIntegrity.fixtureMeta())
            .map { entry -> entry.kind }
        assertEquals(
            listOf(
                RulesetChangeKind.ANIMAL_DERIVED_TAGS,
                RulesetChangeKind.PRESET_CAUTION_TAGS,
                RulesetChangeKind.ALLERGEN_AND_MICROPLASTIC_TAGS,
                RulesetChangeKind.PACKED_SNAPSHOT
            ),
            kinds
        )
    }

    @Test
    fun packedSnapshotUsesDeviceCatalogVersion() {
        val meta: CatalogMeta = CatalogIntegrity.fixtureMeta()
        val packed: RulesetChange = RulesetChangelog.entries(meta)
            .first { entry -> entry.kind == RulesetChangeKind.PACKED_SNAPSHOT }
        assertEquals(meta.catalogVersion, packed.versionLabel)
    }

    @Test
    fun missingMetaOmitsPackedSnapshot() {
        val entries: List<RulesetChange> = RulesetChangelog.entries(null)
        assertTrue(entries.none { entry -> entry.kind == RulesetChangeKind.PACKED_SNAPSHOT })
        assertTrue(entries.all { entry -> entry.versionLabel == null })
        assertEquals(3, entries.size)
    }
}
