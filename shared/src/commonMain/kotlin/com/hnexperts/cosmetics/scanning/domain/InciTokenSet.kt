package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

object InciTokenSet {
    fun equal(left: String, right: String): Boolean {
        return normalizeSet(left) == normalizeSet(right)
    }

    fun normalizeSet(raw: String): Set<String> {
        return raw.split(',')
            .map { token -> InciNormalizer.normalize(token) }
            .filter { token -> token.isNotEmpty() }
            .toSet()
    }
}
