package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.withContext

class UrlConnectionHttpClient(
    private val dispatchers: AppDispatchers
) : SimpleHttpClient {
    override suspend fun getText(url: String): Outcome<String> {
        return FailureCatcher.network("http.get") {
            withContext(dispatchers.io) {
                fetch(url)
            }
        }
    }

    private fun fetch(url: String): String {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            val code: Int = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body: String = stream.bufferedReader().use { reader -> reader.readText() }
            if (code !in 200..299 && code != 404) {
                throw IllegalStateException("HTTP $code for $url")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS: Int = 20000
        const val USER_AGENT: String = "INCIScan/1.0 (cosmetics ingredient scanner)"
    }
}
