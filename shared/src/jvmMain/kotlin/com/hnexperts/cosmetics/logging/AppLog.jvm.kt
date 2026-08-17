package com.hnexperts.cosmetics.logging

internal actual fun platformLogError(tag: String, message: String, error: Throwable?) {
    printLine("ERROR", tag, message, error)
}

internal actual fun platformLogWarn(tag: String, message: String, error: Throwable?) {
    printLine("WARN", tag, message, error)
}

internal actual fun platformLogInfo(tag: String, message: String) {
    printLine("INFO", tag, message, null)
}

private fun printLine(level: String, tag: String, message: String, error: Throwable?) {
    val suffix: String = if (error == null) "" else " | ${error.stackTraceToString()}"
    println("[$level][$tag] $message$suffix")
}
