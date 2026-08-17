package com.hnexperts.cosmetics.logging

import platform.Foundation.NSLog

internal actual fun platformLogError(tag: String, message: String, error: Throwable?) {
    nsLog("ERROR", tag, message, error)
}

internal actual fun platformLogWarn(tag: String, message: String, error: Throwable?) {
    nsLog("WARN", tag, message, error)
}

internal actual fun platformLogInfo(tag: String, message: String) {
    nsLog("INFO", tag, message, null)
}

private fun nsLog(level: String, tag: String, message: String, error: Throwable?) {
    val suffix: String = if (error == null) "" else " | ${error.message}"
    NSLog("%@", "[$level][$tag] $message$suffix")
}
