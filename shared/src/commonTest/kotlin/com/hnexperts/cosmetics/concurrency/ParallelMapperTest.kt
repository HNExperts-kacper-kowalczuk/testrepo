package com.hnexperts.cosmetics.concurrency

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelMapperTest {
    @Test
    fun preservesOrderBelowThreshold() {
        val input: List<Int> = (1..5).toList()
        val output: List<Int> = runBlocking {
            ParallelMapper.map(input, threshold = 8) { value -> value * 2 }
        }
        assertEquals(listOf(2, 4, 6, 8, 10), output)
    }

    @Test
    fun preservesOrderWhenChunkedAcrossWorkers() {
        val input: List<Int> = (0 until 20).toList()
        val output: List<Int> = runBlocking {
            ParallelMapper.map(input, threshold = 8, workerCount = 4) { value -> value + 1 }
        }
        assertEquals(input.map { value -> value + 1 }, output)
    }
}
