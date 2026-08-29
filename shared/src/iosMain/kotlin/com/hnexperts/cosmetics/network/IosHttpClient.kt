package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.catalog.application.CatalogSyncPaths
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setValue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
class IosHttpClient(
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

    private suspend fun fetchText(url: String): String {
        val result: HttpBody = fetch(url, method = "GET", jsonBody = null)
        if (result.code !in 200..299 && result.code != 404) {
            throw IllegalStateException("HTTP ${result.code} for $url")
        }
        return result.bytes.decodeToString()
    }

    private suspend fun fetchBytes(url: String): ByteArray {
        val result: HttpBody = fetch(url, method = "GET", jsonBody = null)
        if (result.bytes.size > CatalogSyncPaths.MAX_BYTES) {
            throw IllegalStateException("HTTP body exceeds ${CatalogSyncPaths.MAX_BYTES} bytes")
        }
        if (result.code !in 200..299) {
            throw IllegalStateException("HTTP ${result.code} for $url")
        }
        return result.bytes
    }

    private suspend fun post(url: String, body: String) {
        val result: HttpBody = fetch(url, method = "POST", jsonBody = body)
        if (result.code !in 200..299) {
            throw IllegalStateException("HTTP ${result.code} for $url")
        }
    }

    private suspend fun fetch(url: String, method: String, jsonBody: String?): HttpBody {
        val nsUrl: NSURL = NSURL.URLWithString(url)
            ?: throw IllegalStateException("Invalid URL: $url")
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.HTTPMethod = method
        request.setValue(USER_AGENT, forHTTPHeaderField = "User-Agent")
        if (jsonBody != null) {
            request.setValue("application/json; charset=utf-8", forHTTPHeaderField = "Content-Type")
            request.HTTPBody = jsonBody.encodeToByteArray().toNSData()
        }
        return suspendCancellableCoroutine { continuation ->
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) {
                    data: NSData?,
                    response: NSURLResponse?,
                    error: NSError? ->
                when {
                    error != null -> continuation.resumeWithException(
                        IllegalStateException(error.localizedDescription)
                    )
                    data == null -> continuation.resumeWithException(IllegalStateException("Empty HTTP body"))
                    else -> {
                        val code: Int = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                        continuation.resume(HttpBody(code = code, bytes = data.toByteArray()))
                    }
                }
            }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }
    }

    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) {
            return NSData()
        }
        return usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) ?: NSData()
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        val pointer = bytes ?: return ByteArray(0)
        return pointer.readBytes(length.toInt())
    }

    private class HttpBody(
        val code: Int,
        val bytes: ByteArray
    )

    private companion object {
        const val USER_AGENT: String = "INCIScan/1.0 (cosmetics ingredient scanner)"
    }
}
