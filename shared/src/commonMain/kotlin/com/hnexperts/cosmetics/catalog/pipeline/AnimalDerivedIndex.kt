package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Incomplete animal-derived INCI set (carmine, keratin, beeswax, and similar).
 * A catalog tag for a chip, not a vegan certification and not a hazard score.
 * Applied at CosIng ingest and when assembling the in-memory index.
 */
object AnimalDerivedIndex {
    const val TAG: String = "ANIMAL_DERIVED"

    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty() || !NAMES.contains(key)) {
            return emptyList()
        }
        return listOf(TAG)
    }

    private val NAMES: Set<String> = normalizeAll(
        "Carmine",
        "CI 75470",
        "Keratin",
        "Hydrolyzed Keratin",
        "Lactose",
        "Beeswax",
        "Cera Alba",
        "Lanolin",
        "Lanolin Alcohol",
        "Squalene",
        "Collagen",
        "Hydrolyzed Collagen",
        "Gelatin",
        "Shellac"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
