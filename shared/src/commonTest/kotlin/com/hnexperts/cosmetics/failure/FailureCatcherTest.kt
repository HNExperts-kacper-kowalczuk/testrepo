package com.hnexperts.cosmetics.failure

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FailureCatcherTest {
    @Test
    fun mapsExceptionToDatabaseFailure() {
        val outcome: Outcome<Int> = runBlocking {
            FailureCatcher.database("history.recent") {
                throw IllegalStateException("disk full")
            }
        }
        val failure: AppFailure = (outcome as Outcome.Err).failure
        assertTrue(failure is AppFailure.Database)
        assertEquals("history.recent", failure.operation)
        assertTrue(failure.detail.contains("IllegalStateException"))
        assertTrue(failure.detail.contains("disk full"))
    }

    @Test
    fun rethrowsCancellation() {
        val cancelled: Boolean = runBlocking {
            try {
                coroutineScope {
                    val child = async {
                        FailureCatcher.database("catalog.search") {
                            throw CancellationException("stop")
                        }
                    }
                    child.await()
                }
                false
            } catch (error: CancellationException) {
                true
            }
        }
        assertTrue(cancelled)
    }
}

class OutcomeZipTest {
    @Test
    fun zipReturnsFirstError() {
        val left: Outcome<Int> = Outcome.Err(AppFailure.Database("a", "left"))
        val right: Outcome<String> = Outcome.Ok("ok")
        val zipped: Outcome<Pair<Int, String>> = Outcome.zip(left, right)
        assertEquals("a", (zipped as Outcome.Err).failure.operation)
    }
}
