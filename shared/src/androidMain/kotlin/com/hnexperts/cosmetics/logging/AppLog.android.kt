package com.hnexperts.cosmetics.logging

import android.util.Log

internal actual fun platformLogError(tag: String, message: String, error: Throwable?) {
    if (error == null) {
        Log.e(tag, message)
    } else {
        Log.e(tag, message, error)
    }
}

internal actual fun platformLogWarn(tag: String, message: String, error: Throwable?) {
    if (error == null) {
        Log.w(tag, message)
    } else {
        Log.w(tag, message, error)
    }
}

internal actual fun platformLogInfo(tag: String, message: String) {
    Log.i(tag, message)
}
