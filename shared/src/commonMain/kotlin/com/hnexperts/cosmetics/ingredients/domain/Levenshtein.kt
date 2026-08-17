package com.hnexperts.cosmetics.ingredients.domain

object Levenshtein {
    fun distance(left: String, right: String): Int {
        if (left == right) {
            return 0
        }
        if (left.isEmpty()) {
            return right.length
        }
        if (right.isEmpty()) {
            return left.length
        }
        val previous: IntArray = IntArray(right.length + 1) { index -> index }
        val current: IntArray = IntArray(right.length + 1)
        for (i in left.indices) {
            current[0] = i + 1
            for (j in right.indices) {
                val cost: Int = if (left[i] == right[j]) 0 else 1
                current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + cost)
            }
            for (j in previous.indices) {
                previous[j] = current[j]
            }
        }
        return previous[right.length]
    }
}
