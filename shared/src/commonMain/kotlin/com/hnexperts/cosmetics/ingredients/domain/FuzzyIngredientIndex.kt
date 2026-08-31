package com.hnexperts.cosmetics.ingredients.domain

import com.hnexperts.cosmetics.concurrency.ParallelMapper
import kotlin.math.abs

internal class FuzzyIngredientIndex(
    ingredients: Collection<Ingredient>,
    aliasToIngredient: Map<String, Ingredient>
) {
    private val names: List<IndexedName> = ingredients.map { ingredient ->
        IndexedName(ingredient, InciNormalizer.normalize(ingredient.inciName))
    }
    private val aliases: List<IndexedName> = aliasToIngredient.map { (alias, ingredient) ->
        IndexedName(ingredient, alias)
    }
    val size: Int = names.size + aliases.size

    fun find(normalized: String): Ingredient? {
        return findHit(normalized)?.ingredient
    }

    fun findHit(normalized: String): FuzzyHit? {
        if (normalized.length < MIN_FUZZY_LENGTH) {
            return null
        }
        return hitFrom(topN(normalized, names + aliases, UNIQUE_RIVAL_LIMIT))
    }

    fun findHits(normalized: String, limit: Int): List<FuzzyHit> {
        if (normalized.length < MIN_FUZZY_LENGTH || limit <= 0) {
            return emptyList()
        }
        val ranked: List<RankedName> = topN(normalized, names + aliases, limit)
        val unique: Boolean = ranked.size == 1
        return ranked.map { item ->
            FuzzyHit(
                ingredient = item.indexed.ingredient,
                distance = item.distance,
                unique = unique
            )
        }
    }

    suspend fun findParallel(normalized: String): Ingredient? {
        return findHitParallel(normalized)?.ingredient
    }

    suspend fun findHitParallel(normalized: String): FuzzyHit? {
        if (normalized.length < MIN_FUZZY_LENGTH) {
            return null
        }
        val candidates: List<IndexedName> = names + aliases
        if (candidates.size < PARALLEL_CANDIDATE_THRESHOLD) {
            return findHit(normalized)
        }
        val chunkHits: List<List<RankedName>> = ParallelMapper.map(
            items = candidates.chunked(fuzzyChunkSize(candidates.size)),
            threshold = 2,
            workerCount = ParallelMapper.DEFAULT_WORKER_COUNT
        ) { chunk -> topN(normalized, chunk, UNIQUE_RIVAL_LIMIT) }
        return hitFrom(bestDistinct(chunkHits.flatten(), UNIQUE_RIVAL_LIMIT))
    }

    private fun topN(
        normalized: String,
        candidates: List<IndexedName>,
        limit: Int
    ): List<RankedName> {
        val maxDistance: Int = maxDistanceFor(normalized)
        val ranked: MutableList<RankedName> = mutableListOf()
        for (candidate in candidates) {
            if (abs(normalized.length - candidate.normalized.length) > maxDistance) {
                continue
            }
            val distance: Int = Levenshtein.distance(normalized, candidate.normalized)
            if (distance > maxDistance) {
                continue
            }
            insertRanked(ranked, RankedName(candidate, distance), limit)
        }
        return ranked
    }

    private fun insertRanked(ranked: MutableList<RankedName>, incoming: RankedName, limit: Int) {
        val sameId: Int = ranked.indexOfFirst { item ->
            item.indexed.ingredient.id == incoming.indexed.ingredient.id
        }
        if (sameId >= 0) {
            replaceIfCloser(ranked, sameId, incoming)
            return
        }
        ranked.add(incoming)
        ranked.sortBy { item -> item.distance }
        trimToLimit(ranked, limit)
    }

    private fun replaceIfCloser(ranked: MutableList<RankedName>, index: Int, incoming: RankedName) {
        if (incoming.distance >= ranked[index].distance) {
            return
        }
        ranked[index] = incoming
        ranked.sortBy { item -> item.distance }
    }

    private fun trimToLimit(ranked: MutableList<RankedName>, limit: Int) {
        while (ranked.size > limit) {
            ranked.removeAt(ranked.lastIndex)
        }
    }

    private fun bestDistinct(ranked: List<RankedName>, limit: Int): List<RankedName> {
        val compact: MutableList<RankedName> = mutableListOf()
        for (item in ranked.sortedBy { rankedName -> rankedName.distance }) {
            insertRanked(compact, item, limit)
        }
        return compact
    }

    private fun hitFrom(ranked: List<RankedName>): FuzzyHit? {
        val best: RankedName = ranked.firstOrNull() ?: return null
        return FuzzyHit(
            ingredient = best.indexed.ingredient,
            distance = best.distance,
            unique = ranked.size == 1
        )
    }

    private fun maxDistanceFor(normalized: String): Int {
        return if (normalized.length < 8) 1 else 2
    }

    private fun fuzzyChunkSize(candidateCount: Int): Int {
        val workers: Int = ParallelMapper.DEFAULT_WORKER_COUNT
        return (candidateCount + workers - 1) / workers
    }

    private data class IndexedName(
        val ingredient: Ingredient,
        val normalized: String
    )

    private data class RankedName(
        val indexed: IndexedName,
        val distance: Int
    )

    private companion object {
        const val MIN_FUZZY_LENGTH: Int = 5
        const val UNIQUE_RIVAL_LIMIT: Int = 2
        const val PARALLEL_CANDIDATE_THRESHOLD: Int = 256
    }
}
