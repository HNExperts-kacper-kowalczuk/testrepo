package com.hnexperts.cosmetics.ui

import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.failure.toVerboseString
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CancellationException

suspend fun <T> runUiAction(
    onFailure: (AppFailure) -> Unit,
    block: suspend () -> Outcome<T>
): T? {
    return try {
        when (val result: Outcome<T> = block()) {
            is Outcome.Ok -> result.value
            is Outcome.Err -> {
                onFailure(result.failure)
                null
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        AppLog.e("ui.action", error.toVerboseString(), error)
        onFailure(AppFailure.Unexpected(operation = "ui.action", detail = error.toVerboseString()))
        null
    }
}
