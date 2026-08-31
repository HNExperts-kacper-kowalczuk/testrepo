package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.FuzzyHit

object FuzzyAutoAccept {
    const val MIN_TOKEN_LENGTH: Int = 8
    const val MAX_DISTANCE: Int = 2

    fun decision(hit: FuzzyHit, normalizedLength: Int): FuzzyDecision {
        if (shouldAccept(hit, normalizedLength)) {
            return FuzzyDecision.AUTO_ACCEPTED
        }
        return FuzzyDecision.PENDING
    }

    fun shouldAccept(hit: FuzzyHit, normalizedLength: Int): Boolean {
        if (hit.distance > MAX_DISTANCE) {
            return false
        }
        if (normalizedLength < MIN_TOKEN_LENGTH) {
            return false
        }
        return hit.unique
    }
}
