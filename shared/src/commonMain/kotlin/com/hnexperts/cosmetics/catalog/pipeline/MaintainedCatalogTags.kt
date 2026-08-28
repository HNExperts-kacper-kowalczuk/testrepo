package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard

/**
 * Tag lists that live in code (EU labelled allergens, microplastics).
 * Ingest writes them into a new pack; assemble still unions them so a
 * gzip packed before those indexes evaluates the same way.
 */
object MaintainedCatalogTags {
    fun applyTo(snapshot: CatalogSnapshot): CatalogSnapshot {
        val namesById: Map<String, String> = snapshot.ingredients.associate { ingredient ->
            ingredient.id to ingredient.inciName
        }
        return snapshot.copy(hazards = mergeHazards(snapshot.hazards, namesById))
    }

    fun merge(inciName: String, existing: List<String>): List<String> {
        val extra: List<String> = EuLabelledAllergenIndex.tagsFor(inciName) +
            MicroplasticIndex.tagsFor(inciName)
        if (extra.isEmpty()) {
            return existing
        }
        return (existing + extra).distinct()
    }

    private fun mergeHazards(
        hazards: Map<String, IngredientHazard>,
        namesById: Map<String, String>
    ): Map<String, IngredientHazard> {
        return hazards.mapValues { entry -> taggedHazard(entry.value, namesById[entry.key]) }
    }

    private fun taggedHazard(hazard: IngredientHazard, inciName: String?): IngredientHazard {
        if (inciName == null) {
            return hazard
        }
        val merged: List<String> = merge(inciName, hazard.regulatoryTags)
        if (merged == hazard.regulatoryTags) {
            return hazard
        }
        return hazard.copy(regulatoryTags = merged)
    }
}
