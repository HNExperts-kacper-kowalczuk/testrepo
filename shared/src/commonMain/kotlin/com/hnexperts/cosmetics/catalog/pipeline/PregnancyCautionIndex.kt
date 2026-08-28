package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Incomplete pregnancy-caution INCI set used by the pregnancy preset.
 * Not a diagnosis. Applied at CosIng ingest and when assembling.
 */
object PregnancyCautionIndex {
    const val TAG: String = "PREGNANCY_CAUTION"

    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty() || !NAMES.contains(key)) {
            return emptyList()
        }
        return listOf(TAG)
    }

    private val NAMES: Set<String> = normalizeAll(
        "Retinol",
        "Retinal",
        "Retinyl Palmitate",
        "Retinyl Acetate",
        "Salicylic Acid"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
