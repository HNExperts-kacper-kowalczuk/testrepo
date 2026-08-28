package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Incomplete “not for young children” INCI set used by the children-caution
 * preset. Not a diagnosis. Applied at CosIng ingest and when assembling.
 */
object ChildrenCautionIndex {
    const val TAG: String = "CHILDREN"

    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty() || !NAMES.contains(key)) {
            return emptyList()
        }
        return listOf(TAG)
    }

    private val NAMES: Set<String> = normalizeAll(
        "Salicylic Acid",
        "Methyl Salicylate"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
