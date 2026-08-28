package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Incomplete sun-caution list (furocoumarin citrus peel oils and the
 * fixture keratolytic). A catalog tag for the finding badge, not a diagnosis
 * and not a hazard score. Applied at CosIng ingest and when assembling.
 */
object PhototoxicIndex {
    const val TAG: String = "PHOTOTOXIC"

    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty() || !NAMES.contains(key)) {
            return emptyList()
        }
        return listOf(TAG)
    }

    private val NAMES: Set<String> = normalizeAll(
        "Salicylic Acid",
        "Citrus Aurantium Bergamia Peel Oil",
        "Citrus Limon Peel Oil",
        "Citrus Aurantifolia Peel Oil",
        "Citrus Paradisi Peel Oil",
        "Citrus Aurantium Peel Oil"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
