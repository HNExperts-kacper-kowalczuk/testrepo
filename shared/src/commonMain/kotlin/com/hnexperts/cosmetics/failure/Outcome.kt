package com.hnexperts.cosmetics.failure

sealed class Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>()
    data class Err(val failure: AppFailure) : Outcome<Nothing>()

    fun getOrNull(): T? {
        return when (this) {
            is Ok -> value
            is Err -> null
        }
    }

    companion object {
        fun <A, B> zip(left: Outcome<A>, right: Outcome<B>): Outcome<Pair<A, B>> {
            if (left is Err) {
                return left
            }
            if (right is Err) {
                return right
            }
            return Ok((left as Ok).value to (right as Ok).value)
        }
    }
}
