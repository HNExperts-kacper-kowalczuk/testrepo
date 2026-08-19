package com.hnexperts.cosmetics.preferences.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage

data class UserAvoidanceProfile(
    val pregnancyCaution: Boolean,
    val fragranceFree: Boolean,
    val avoidedIngredientIds: Set<String>,
    val euAllergens: Boolean = false,
    val childrenCaution: Boolean = false,
    val alcoholLeaveOn: Boolean = false,
    val essentialOilCluster: Boolean = false
) {
    fun avoids(
        ingredientId: String,
        functionTags: List<String>,
        regulatoryTags: List<String>,
        usage: ProductUsage = ProductUsage.UNKNOWN
    ): Boolean {
        if (avoidedIngredientIds.contains(ingredientId)) {
            return true
        }
        if (fragranceFree && functionTags.contains(TAG_FRAGRANCE)) {
            return true
        }
        if (pregnancyCaution && regulatoryTags.contains(TAG_PREGNANCY_CAUTION)) {
            return true
        }
        if (euAllergens && regulatoryTags.contains(TAG_ALLERGEN_26)) {
            return true
        }
        if (childrenCaution && regulatoryTags.contains(TAG_CHILDREN)) {
            return true
        }
        if (alcoholLeaveOn && isAlcoholLeaveOnHit(ingredientId, usage)) {
            return true
        }
        if (essentialOilCluster && isEssentialOilHit(ingredientId, functionTags)) {
            return true
        }
        return false
    }

    private fun isAlcoholLeaveOnHit(ingredientId: String, usage: ProductUsage): Boolean {
        if (!ALCOHOL_DENAT_IDS.contains(ingredientId)) {
            return false
        }
        return usage.scoringUsage() != ProductUsage.RINSE_OFF
    }

    private fun isEssentialOilHit(ingredientId: String, functionTags: List<String>): Boolean {
        if (ESSENTIAL_OIL_IDS.contains(ingredientId)) {
            return true
        }
        return functionTags.contains(TAG_ESSENTIAL_OIL)
    }

    companion object {
        const val TAG_FRAGRANCE: String = "FRAGRANCE"
        const val TAG_PREGNANCY_CAUTION: String = "PREGNANCY_CAUTION"
        const val TAG_ALLERGEN_26: String = "ALLERGEN_26"
        const val TAG_CHILDREN: String = "CHILDREN"
        const val TAG_ESSENTIAL_OIL: String = "ESSENTIAL_OIL"

        val ALCOHOL_DENAT_IDS: Set<String> = setOf("alcohol-denat")
        val ESSENTIAL_OIL_IDS: Set<String> = setOf(
            "limonene",
            "linalool",
            "citral",
            "geraniol",
            "eugenol"
        )

        val EMPTY: UserAvoidanceProfile = UserAvoidanceProfile(
            pregnancyCaution = false,
            fragranceFree = false,
            avoidedIngredientIds = emptySet()
        )
    }
}
