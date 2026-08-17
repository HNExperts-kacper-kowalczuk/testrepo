package com.hnexperts.cosmetics.ingredients.domain

object InciNormalizer {
    fun normalize(raw: String): String {
        val collapsed: String = raw.trim().replace(WHITESPACE, " ")
        return collapsed.uppercase()
    }

    fun stripParenthetical(normalized: String): Pair<String, String?> {
        val openIndex: Int = normalized.lastIndexOf('(')
        val closeIndex: Int = normalized.lastIndexOf(')')
        if (openIndex <= 0 || closeIndex != normalized.length - 1 || closeIndex <= openIndex) {
            return Pair(normalized, null)
        }
        val outer: String = normalized.substring(0, openIndex).trim()
        val inner: String = normalized.substring(openIndex + 1, closeIndex).trim()
        if (outer.isEmpty() || inner.isEmpty()) {
            return Pair(normalized, null)
        }
        return Pair(outer, inner)
    }

    private val WHITESPACE: Regex = Regex("\\s+")
}
