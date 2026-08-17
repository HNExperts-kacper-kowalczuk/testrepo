package com.hnexperts.cosmetics.failure

fun Throwable.toVerboseString(): String {
    return generateSequence(this) { error -> error.cause }
        .joinToString(separator = " → ") { error ->
            val name: String = error::class.simpleName ?: "Throwable"
            val message: String = error.message?.ifBlank { null } ?: "no message"
            "$name: $message"
        }
}
