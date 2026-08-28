package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.catalog.application.CatalogSyncPaths
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.withContext

class UrlConnectionHttpClient(
    private val dispatchers: AppDispatchers
) : SimpleHttpClient {
    override suspend fun getText(url: String): Outcome<String> {
        return FailureCatcher.network("http.get") {
            withContext(dispatchers.io) {
                fetchText(url)
            }
        }
    }

    override suspend fun getBytes(url: String): Outcome<ByteArray> {
        return FailureCatcher.network("http.get.bytes") {
            withContext(dispatchers.io) {
                fetchBytes(url)
            }
        }
    }

    override suspend fun postJson(url: String, body: String): Outcome<Unit> {
        return FailureCatcher.network("http.post") {
            withContext(dispatchers.io) {
                post(url, body)
            }
        }
    }

    private fun fetchText(url: String): String {
        val connection: HttpURLConnection = open(url)
        try {
            val code: Int = connection.responseCode
            val body: String = responseStream(connection, code).bufferedReader().use { reader ->
                reader.readText()
            }
            if (code !in 200..299 && code != 404) {
                throw IllegalStateException("HTTP $code for $url")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchBytes(url: String): ByteArray {
        val connection: HttpURLConnection = open(url)
        try {
            rejectOversized(connection)
            val code: Int = connection.responseCode
            val body: ByteArray = responseStream(connection, code).readAtMost(CatalogSyncPaths.MAX_BYTES)
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code for $url")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun post(url: String, body: String) {
        val connection: HttpURLConnection = open(url)
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { stream ->
                stream.write(body.encodeToByteArray())
            }
            val code: Int = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code for $url")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: String): HttpURLConnection {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", "application/json")
        return connection
    }

    private fun rejectOversized(connection: HttpURLConnection) {
        val length: Long = connection.contentLengthLong
        if (length > CatalogSyncPaths.MAX_BYTES) {
            throw IllegalStateException("HTTP body exceeds ${CatalogSyncPaths.MAX_BYTES} bytes")
        }
    }

    private fun responseStream(connection: HttpURLConnection, code: Int): InputStream {
        return if (code in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }
    }

    private fun InputStream.readAtMost(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val chunk: ByteArray = ByteArray(CHUNK)
        var total: Int = 0
        while (true) {
            val count: Int = read(chunk)
            if (count < 0) {
                break
            }
            total += count
            if (total > maxBytes) {
                throw IllegalStateException("HTTP body exceeds $maxBytes bytes")
            }
            output.write(chunk, 0, count)
        }
        return output.toByteArray()
    }

    private companion object {
        const val TIMEOUT_MS: Int = 20000
        const val CHUNK: Int = 8192
        const val USER_AGENT: String = "INCIScan/1.0 (cosmetics ingredient scanner)"
    }
}
