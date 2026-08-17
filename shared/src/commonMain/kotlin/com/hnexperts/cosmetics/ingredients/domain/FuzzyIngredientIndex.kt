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
        if (normalized.length < MIN_FUZZY_LENGTH) {
            return null
        }
        return bestOf(normalized, names + aliases)
    }

    suspend fun findParallel(normalized: String): Ingredient? {
        if (normalized.length < MIN_FUZZY_LENGTH) {
            return null
        }
        val candidates: List<IndexedName> = names + aliases
        if (candidates.size < PARALLEL_CANDIDATE_THRESHOLD) {
            return bestOf(normalized, candidates)
        }
        val chunkHits: List<IndexedName?> = ParallelMapper.map(
            items = candidates.chunked(fuzzyChunkSize(candidates.size)),
            threshold = 2,
            workerCount = ParallelMapper.DEFAULT_WORKER_COUNT
        ) { chunk -> bestIndexed(normalized, chunk) }
        return closest(chunkHits.filterNotNull(), normalized)?.ingredient
    }

    private fun bestOf(normalized: String, candidates: List<IndexedName>): Ingredient? {
        return bestIndexed(normalized, candidates)?.ingredient
    }

    private fun bestIndexed(normalized: String, candidates: List<IndexedName>): IndexedName? {
        val maxDistance: Int = maxDistanceFor(normalized)
        var best: IndexedName? = null
        var bestDistance: Int = maxDistance + 1
        for (candidate in candidates) {
            if (abs(normalized.length - candidate.normalized.length) > maxDistance) {
                continue
            }
            val distance: Int = Levenshtein.distance(normalized, candidate.normalized)
            if (distance < bestDistance && distance <= maxDistance) {
                best = candidate
                bestDistance = distance
            }
        }
        return best
    }

    private fun closest(hits: List<IndexedName>, normalized: String): IndexedName? {
        val maxDistance: Int = maxDistanceFor(normalized)
        var best: IndexedName? = null
        var bestDistance: Int = maxDistance + 1
        for (hit in hits) {
            val distance: Int = Levenshtein.distance(normalized, hit.normalized)
            if (distance < bestDistance && distance <= maxDistance) {
                best = hit
                bestDistance = distance
            }
        }
        return best
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

    private companion object {
        const val MIN_FUZZY_LENGTH: Int = 5
        const val PARALLEL_CANDIDATE_THRESHOLD: Int = 256
    }
}
