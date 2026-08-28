package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Solid synthetic polymers commonly used as microbeads or powders.
 * This list is incomplete; it is a catalog tag, not a hazard score.
 */
object MicroplasticIndex {
    const val TAG: String = "MICROPLASTIC"

    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty() || !NAMES.contains(key)) {
            return emptyList()
        }
        return listOf(TAG)
    }

    private val NAMES: Set<String> = normalizeAll(
        "Polyethylene",
        "Polypropylene",
        "Nylon-6",
        "Nylon-12",
        "Polymethyl Methacrylate",
        "Polytetrafluoroethylene"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
