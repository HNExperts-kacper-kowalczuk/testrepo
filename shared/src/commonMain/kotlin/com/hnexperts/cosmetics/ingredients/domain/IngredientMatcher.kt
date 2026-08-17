package com.hnexperts.cosmetics.ingredients.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class IngredientMatcher(
    ingredients: Collection<Ingredient>,
    aliases: Map<String, String>,
    commaExceptions: Collection<String>
) {
    private val tokenizer: InciTokenizer = InciTokenizer(commaExceptions)
    private val byNormalizedName: Map<String, Ingredient> = ingredients.associateBy { ingredient ->
        InciNormalizer.normalize(ingredient.inciName)
    }
    private val byId: Map<String, Ingredient> = ingredients.associateBy { ingredient -> ingredient.id }
    private val aliasToIngredient: Map<String, Ingredient> = aliases.mapNotNull { (alias, ingredientId) ->
        val ingredient: Ingredient = byId[ingredientId] ?: return@mapNotNull null
        InciNormalizer.normalize(alias) to ingredient
    }.toMap()

    fun matchList(inciRaw: String): List<IngredientRef> {
        return tokenizer.tokenize(inciRaw).map { token -> matchToken(token) }
    }

    fun matchToken(rawToken: String): IngredientRef {
        val normalized: String = InciNormalizer.normalize(rawToken)
        if (normalized.isEmpty()) {
            return IngredientRef(id = null, displayName = rawToken.trim(), matchedBy = MatchMethod.UNMATCHED)
        }
        val exact: Ingredient? = lookupExact(normalized)
        if (exact != null) {
            return IngredientRef(id = exact.id, displayName = exact.inciName, matchedBy = MatchMethod.EXACT)
        }
        val alias: Ingredient? = aliasToIngredient[normalized]
        if (alias != null) {
            return IngredientRef(id = alias.id, displayName = alias.inciName, matchedBy = MatchMethod.ALIAS)
        }
        val parenthetical: Pair<String, String?> = InciNormalizer.stripParenthetical(normalized)
        val outerMatch: Ingredient? = lookupExact(parenthetical.first) ?: aliasToIngredient[parenthetical.first]
        if (outerMatch != null) {
            return IngredientRef(id = outerMatch.id, displayName = outerMatch.inciName, matchedBy = MatchMethod.ALIAS)
        }
        val inner: String? = parenthetical.second
        if (inner != null) {
            val innerMatch: Ingredient? = lookupExact(inner) ?: aliasToIngredient[inner]
            if (innerMatch != null) {
                return IngredientRef(id = innerMatch.id, displayName = innerMatch.inciName, matchedBy = MatchMethod.ALIAS)
            }
        }
        val slashParts: List<String> = normalized.split('/').map { part -> part.trim() }.filter { part -> part.isNotEmpty() }
        if (slashParts.size > 1) {
            for (part in slashParts) {
                val slashMatch: Ingredient? = lookupExact(part) ?: aliasToIngredient[part]
                if (slashMatch != null) {
                    return IngredientRef(id = slashMatch.id, displayName = slashMatch.inciName, matchedBy = MatchMethod.ALIAS)
                }
            }
        }
        val fuzzy: Ingredient? = lookupFuzzy(normalized)
        if (fuzzy != null) {
            return IngredientRef(id = fuzzy.id, displayName = fuzzy.inciName, matchedBy = MatchMethod.FUZZY)
        }
        return IngredientRef(id = null, displayName = rawToken.trim(), matchedBy = MatchMethod.UNMATCHED)
    }

    suspend fun matchListConcurrently(inciRaw: String): List<IngredientRef> {
        val tokens: List<String> = tokenizer.tokenize(inciRaw)
        if (tokens.size < PARALLEL_TOKEN_THRESHOLD) {
            return tokens.map { token -> matchToken(token) }
        }
        return coroutineScope {
            tokens.map { token ->
                async { matchToken(token) }
            }.awaitAll()
        }
    }

    private fun lookupExact(normalized: String): Ingredient? {
        return byNormalizedName[normalized]
    }

    private fun lookupFuzzy(normalized: String): Ingredient? {
        if (normalized.length < MIN_FUZZY_LENGTH) {
            return null
        }
        val maxDistance: Int = if (normalized.length < 8) 1 else 2
        var best: Ingredient? = null
        var bestDistance: Int = maxDistance + 1
        val candidates: Sequence<Ingredient> = (byNormalizedName.values.asSequence() + aliasToIngredient.values.asSequence()).distinct()
        for (ingredient in candidates) {
            val nameDistance: Int = levenshtein(normalized, InciNormalizer.normalize(ingredient.inciName))
            if (nameDistance < bestDistance && nameDistance <= maxDistance) {
                best = ingredient
                bestDistance = nameDistance
            }
        }
        for (alias in aliasToIngredient.keys) {
            val aliasDistance: Int = levenshtein(normalized, alias)
            if (aliasDistance < bestDistance && aliasDistance <= maxDistance) {
                best = aliasToIngredient.getValue(alias)
                bestDistance = aliasDistance
            }
        }
        return best
    }

    private fun levenshtein(left: String, right: String): Int {
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

    private companion object {
        const val MIN_FUZZY_LENGTH: Int = 5
        const val PARALLEL_TOKEN_THRESHOLD: Int = 8
    }
}
