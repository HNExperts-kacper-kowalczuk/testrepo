package com.hnexperts.cosmetics.failure

/**
 * Domain-facing failure. [operation] is a stable code for logs; [detail] is verbose
 * (exception type chain + messages) for support and on-screen diagnostics.
 */
sealed class AppFailure {
    abstract val operation: String
    abstract val detail: String

    fun verboseMessage(): String {
        return "$operation — $detail"
    }

    data class CatalogLoad(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class CorruptCatalog(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Database(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Evaluation(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Camera(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Ocr(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Network(
        override val operation: String,
        override val detail: String
    ) : AppFailure()

    data class Unexpected(
        override val operation: String,
        override val detail: String
    ) : AppFailure()
}
