package com.hnexperts.cosmetics.concurrency

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

object ParallelMapper {
    suspend fun <T, R> map(
        items: List<T>,
        threshold: Int = DEFAULT_THRESHOLD,
        workerCount: Int = DEFAULT_WORKER_COUNT,
        transform: (T) -> R
    ): List<R> {
        if (items.size < threshold || workerCount <= 1) {
            return items.map(transform)
        }
        val workers: Int = minOf(workerCount, items.size)
        val chunkSize: Int = (items.size + workers - 1) / workers
        return coroutineScope {
            items.chunked(chunkSize).map { chunk ->
                async {
                    coroutineContext.ensureActive()
                    chunk.map(transform)
                }
            }.awaitAll().flatten()
        }
    }

    const val DEFAULT_THRESHOLD: Int = 8
    const val DEFAULT_WORKER_COUNT: Int = 4
}
