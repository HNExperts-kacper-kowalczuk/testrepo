package com.hnexperts.cosmetics.logging

object AppLog {
    fun e(tag: String, message: String, error: Throwable? = null) {
        platformLogError(tag, message, error)
    }

    fun w(tag: String, message: String, error: Throwable? = null) {
        platformLogWarn(tag, message, error)
    }

    fun i(tag: String, message: String) {
        platformLogInfo(tag, message)
    }
}

internal expect fun platformLogError(tag: String, message: String, error: Throwable?)
internal expect fun platformLogWarn(tag: String, message: String, error: Throwable?)
internal expect fun platformLogInfo(tag: String, message: String)
