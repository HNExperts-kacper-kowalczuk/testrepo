package com.hnexperts.cosmetics.ingredients.domain

/**
 * Recovers INCI names when OCR or a pack omitted commas and left a
 * space-separated blob such as "AQUA ALCOHOL DENAT. GLYCERIN".
 * Accepts a split only when the whole blob is consumed into two or more
 * dictionary hits. Compound slash names stay one token.
 */
class PackedInciSplitter(phrases: Collection<String>) {
    private val byFirstWord: Map<String, List<List<String>>> = indexByFirstWord(phrases)

    fun split(raw: String): List<String>? {
        val words: List<String> = InciNormalizer.normalize(raw)
            .split(' ')
            .map { word -> InciNormalizer.normalize(word) }
            .filter { word -> word.isNotEmpty() }
        if (words.size < 2) {
            return null
        }
        val recovered: List<String> = consume(words, 0) ?: return null
        if (recovered.size < 2) {
            return null
        }
        return recovered
    }

    private fun consume(words: List<String>, start: Int): List<String>? {
        if (start == words.size) {
            return emptyList()
        }
        val options: List<List<String>> = byFirstWord[words[start]] ?: return null
        for (phrase in options) {
            if (!matchesAt(words, start, phrase)) {
                continue
            }
            val rest: List<String>? = consume(words, start + phrase.size)
            if (rest != null) {
                return listOf(phrase.joinToString(" ")) + rest
            }
        }
        return null
    }

    private fun matchesAt(words: List<String>, start: Int, phrase: List<String>): Boolean {
        if (start + phrase.size > words.size) {
            return false
        }
        for (offset in phrase.indices) {
            if (words[start + offset] != phrase[offset]) {
                return false
            }
        }
        return true
    }

    private companion object {
        fun indexByFirstWord(phrases: Collection<String>): Map<String, List<List<String>>> {
            val grouped: MutableMap<String, MutableList<List<String>>> = mutableMapOf()
            for (phrase in phrases) {
                val words: List<String> = InciNormalizer.normalize(phrase)
                    .split(' ')
                    .map { word -> InciNormalizer.normalize(word) }
                    .filter { word -> word.isNotEmpty() }
                if (words.isEmpty()) {
                    continue
                }
                grouped.getOrPut(words.first()) { mutableListOf() }.add(words)
            }
            return grouped.mapValues { entry ->
                entry.value.distinct().sortedWith(
                    compareByDescending<List<String>> { words -> words.size }
                        .thenByDescending { words -> words.sumOf { word -> word.length } }
                )
            }
        }
    }
}
