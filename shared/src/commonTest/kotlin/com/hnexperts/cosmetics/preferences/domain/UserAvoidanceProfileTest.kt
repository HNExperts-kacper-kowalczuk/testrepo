package com.hnexperts.cosmetics.preferences.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserAvoidanceProfileTest {
    @Test
    fun alcoholPresetMatchesDenatNameWhenIdDiffers() {
        val profile: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(alcoholLeaveOn = true)
        assertTrue(
            profile.avoids(
                ingredientId = "alcohol-denatured-cosing",
                functionTags = emptyList(),
                regulatoryTags = emptyList(),
                usage = ProductUsage.LEAVE_ON,
                inciName = "Alcohol Denat."
            )
        )
        assertFalse(
            profile.avoids(
                ingredientId = "alcohol-denatured-cosing",
                functionTags = emptyList(),
                regulatoryTags = emptyList(),
                usage = ProductUsage.RINSE_OFF,
                inciName = "Alcohol Denat."
            )
        )
    }
}
