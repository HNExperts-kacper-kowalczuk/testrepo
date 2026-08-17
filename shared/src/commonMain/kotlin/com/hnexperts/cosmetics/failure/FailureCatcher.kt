package com.hnexperts.cosmetics.failure

import com.hnexperts.cosmetics.catalog.domain.CorruptCatalogException
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CancellationException

object FailureCatcher {
    suspend fun <T> database(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error -> AppFailure.Database(operation, error.toVerboseString()) }, block)
    }

    suspend fun <T> catalog(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error ->
            if (error is CorruptCatalogException) {
                AppFailure.CorruptCatalog(operation, error.toVerboseString())
            } else {
                AppFailure.CatalogLoad(operation, error.toVerboseString())
            }
        }, block)
    }

    suspend fun <T> evaluation(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error -> AppFailure.Evaluation(operation, error.toVerboseString()) }, block)
    }

    suspend fun <T> camera(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error -> AppFailure.Camera(operation, error.toVerboseString()) }, block)
    }

    suspend fun <T> ocr(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error -> AppFailure.Ocr(operation, error.toVerboseString()) }, block)
    }

    suspend fun <T> unexpected(operation: String, block: suspend () -> T): Outcome<T> {
        return wrap(operation, { error -> AppFailure.Unexpected(operation, error.toVerboseString()) }, block)
    }

    suspend fun <T> wrap(
        operation: String,
        map: (Exception) -> AppFailure,
        block: suspend () -> T
    ): Outcome<T> {
        return try {
            Outcome.Ok(block())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AppLog.e(operation, error.toVerboseString(), error)
            Outcome.Err(map(error))
        }
    }
}
