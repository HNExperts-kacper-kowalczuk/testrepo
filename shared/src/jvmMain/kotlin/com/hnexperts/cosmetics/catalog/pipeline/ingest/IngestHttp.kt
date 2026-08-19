package com.hnexperts.cosmetics.catalog.pipeline.ingest

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/** Minimal HTTP helper for the ingest pipeline: retries with backoff. */
class IngestHttp(
    private val maxAttempts: Int = 4,
    private val backoffMs: Long = 2000
) {
    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    fun getBytes(url: String): ByteArray {
        return send(request(url).GET().build()).body()
    }

    fun postText(url: String): String {
        val request: HttpRequest = request(url)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()
        return String(send(request).body())
    }

    private fun request(url: String): HttpRequest.Builder {
        return HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofMinutes(5))
            .header("User-Agent", USER_AGENT)
    }

    private fun send(request: HttpRequest): HttpResponse<ByteArray> {
        var lastError: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                val response: HttpResponse<ByteArray> =
                    client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                if (response.statusCode() in 200..299) {
                    return response
                }
                lastError = IllegalStateException("HTTP ${response.statusCode()} for ${request.uri()}")
            } catch (interrupted: InterruptedException) {
                throw interrupted
            } catch (error: Exception) {
                lastError = error
            }
            Thread.sleep(backoffMs * attempt)
        }
        throw IllegalStateException("Request failed after $maxAttempts attempts: ${request.uri()}", lastError)
    }

    private companion object {
        const val USER_AGENT: String =
            "cosmetics-catalog-ingest/1.0 (offline catalog build; contact: repository maintainers)"
    }
}
