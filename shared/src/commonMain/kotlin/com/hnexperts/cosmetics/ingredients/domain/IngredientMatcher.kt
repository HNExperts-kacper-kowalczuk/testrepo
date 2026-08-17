package com.hnexperts.cosmetics.ingredients.domain

import com.hnexperts.cosmetics.concurrency.ParallelMapper

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
    private val fuzzyIndex: FuzzyIngredientIndex = FuzzyIngredientIndex(ingredients, aliasToIngredient)

    fun matchList(inciRaw: String): List<IngredientRef> {
        return tokenizer.tokenize(inciRaw).map { token -> matchToken(token) }
    }

    suspend fun matchListConcurrently(inciRaw: String): List<IngredientRef> {
        val tokens: List<String> = tokenizer.tokenize(inciRaw)
        if (tokens.size >= ParallelMapper.DEFAULT_THRESHOLD) {
            return ParallelMapper.map(tokens) { token -> matchToken(token) }
        }
        if (fuzzyIndex.size >= FUZZY_PARALLEL_THRESHOLD) {
            return matchShortListWithParallelFuzzy(tokens)
        }
        return tokens.map { token -> matchToken(token) }
    }

    fun matchToken(rawToken: String): IngredientRef {
        val early: IngredientRef? = matchWithoutFuzzy(rawToken)
        if (early != null) {
            return early
        }
        return finishMatch(rawToken, fuzzyIndex.find(InciNormalizer.normalize(rawToken)))
    }

    private suspend fun matchShortListWithParallelFuzzy(tokens: List<String>): List<IngredientRef> {
        return tokens.map { token -> matchTokenWithParallelFuzzy(token) }
    }

    private suspend fun matchTokenWithParallelFuzzy(token: String): IngredientRef {
        val early: IngredientRef? = matchWithoutFuzzy(token)
        if (early != null) {
            return early
        }
        val fuzzy: Ingredient? = fuzzyIndex.findParallel(InciNormalizer.normalize(token))
        return finishMatch(token, fuzzy)
    }

    private fun matchWithoutFuzzy(rawToken: String): IngredientRef? {
        val normalized: String = InciNormalizer.normalize(rawToken)
        if (normalized.isEmpty()) {
            return unmatched(rawToken)
        }
        exactMatch(normalized)?.let { return it }
        aliasMatch(normalized)?.let { return it }
        parentheticalMatch(normalized)?.let { return it }
        slashMatch(normalized)?.let { return it }
        return null
    }

    private fun finishMatch(rawToken: String, fuzzy: Ingredient?): IngredientRef {
        if (fuzzy != null) {
            return IngredientRef(id = fuzzy.id, displayName = fuzzy.inciName, matchedBy = MatchMethod.FUZZY)
        }
        return unmatched(rawToken)
    }

    private fun exactMatch(normalized: String): IngredientRef? {
        val ingredient: Ingredient = byNormalizedName[normalized] ?: return null
        return IngredientRef(id = ingredient.id, displayName = ingredient.inciName, matchedBy = MatchMethod.EXACT)
    }

    private fun aliasMatch(normalized: String): IngredientRef? {
        val ingredient: Ingredient = aliasToIngredient[normalized] ?: return null
        return IngredientRef(id = ingredient.id, displayName = ingredient.inciName, matchedBy = MatchMethod.ALIAS)
    }

    private fun parentheticalMatch(normalized: String): IngredientRef? {
        val parts: Pair<String, String?> = InciNormalizer.stripParenthetical(normalized)
        val outer: Ingredient? = lookupExactOrAlias(parts.first)
        if (outer != null) {
            return aliased(outer)
        }
        val inner: String = parts.second ?: return null
        val innerMatch: Ingredient? = lookupExactOrAlias(inner)
        return innerMatch?.let(::aliased)
    }

    private fun slashMatch(normalized: String): IngredientRef? {
        val slashParts: List<String> = normalized.split('/')
            .map { part -> part.trim() }
            .filter { part -> part.isNotEmpty() }
        if (slashParts.size <= 1) {
            return null
        }
        for (part in slashParts) {
            val match: Ingredient? = lookupExactOrAlias(part)
            if (match != null) {
                return aliased(match)
            }
        }
        return null
    }

    private fun lookupExactOrAlias(normalized: String): Ingredient? {
        return byNormalizedName[normalized] ?: aliasToIngredient[normalized]
    }

    private fun aliased(ingredient: Ingredient): IngredientRef {
        return IngredientRef(id = ingredient.id, displayName = ingredient.inciName, matchedBy = MatchMethod.ALIAS)
    }

    private fun unmatched(rawToken: String): IngredientRef {
        return IngredientRef(id = null, displayName = rawToken.trim(), matchedBy = MatchMethod.UNMATCHED)
    }

    private companion object {
        const val FUZZY_PARALLEL_THRESHOLD: Int = 256
    }
}
