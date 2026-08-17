package com.hnexperts.cosmetics.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Process-wide dispatchers. Catalog and user SQLite files each get their own
 * single-thread limiter so a history write cannot stall a barcode lookup.
 */
class AppDispatchers(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val computation: CoroutineDispatcher = Dispatchers.Default,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val catalogDatabase: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
    val userDatabase: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)
)
