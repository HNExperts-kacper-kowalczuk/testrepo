package com.hnexperts.cosmetics.preferences.domain

data class UserAvoidanceProfile(
    val pregnancyCaution: Boolean,
    val fragranceFree: Boolean,
    val avoidedIngredientIds: Set<String>
) {
    fun avoids(ingredientId: String, functionTags: List<String>, regulatoryTags: List<String>): Boolean {
        if (avoidedIngredientIds.contains(ingredientId)) {
            return true
        }
        if (fragranceFree && functionTags.contains(TAG_FRAGRANCE)) {
            return true
        }
        if (pregnancyCaution && regulatoryTags.contains(TAG_PREGNANCY_CAUTION)) {
            return true
        }
        return false
    }

    companion object {
        const val TAG_FRAGRANCE: String = "FRAGRANCE"
        const val TAG_PREGNANCY_CAUTION: String = "PREGNANCY_CAUTION"

        val EMPTY: UserAvoidanceProfile = UserAvoidanceProfile(
            pregnancyCaution = false,
            fragranceFree = false,
            avoidedIngredientIds = emptySet()
        )
    }
}
