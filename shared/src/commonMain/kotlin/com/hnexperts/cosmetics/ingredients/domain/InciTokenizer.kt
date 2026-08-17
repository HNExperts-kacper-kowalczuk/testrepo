package com.hnexperts.cosmetics.ingredients.domain

class InciTokenizer(
    commaExceptions: Collection<String>
) {
    private val exceptionsLongestFirst: List<String> = commaExceptions
        .map(InciNormalizer::normalize)
        .distinct()
        .sortedByDescending { exception -> exception.length }

    fun tokenize(inciRaw: String): List<String> {
        val normalized: String = InciNormalizer.normalize(inciRaw)
        if (normalized.isEmpty()) {
            return emptyList()
        }
        val protectedText: String = protectExceptions(normalized)
        return protectedText
            .split(TOKEN_SPLIT)
            .map { token -> restoreExceptions(token.trim()) }
            .filter { token -> token.isNotEmpty() }
    }

    private fun protectExceptions(text: String): String {
        var current: String = text
        for ((index, exception) in exceptionsLongestFirst.withIndex()) {
            current = current.replace(exception, sentinel(index))
        }
        return current
    }

    private fun restoreExceptions(token: String): String {
        var current: String = token
        for ((index, exception) in exceptionsLongestFirst.withIndex()) {
            current = current.replace(sentinel(index), exception)
        }
        return current
    }

    private fun sentinel(index: Int): String {
        return "\u0001EX$index\u0001"
    }

    private companion object {
        val TOKEN_SPLIT: Regex = Regex("[,\\n;]+")
    }
}
