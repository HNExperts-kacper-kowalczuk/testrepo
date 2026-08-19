package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
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
                fetch(url)
            }
        }
    }

    private suspend fun fetch(url: String): String {
        val nsUrl: NSURL = NSURL.URLWithString(url)
            ?: throw IllegalStateException("Invalid URL: $url")
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setValue(USER_AGENT, forHTTPHeaderField = "User-Agent")
        return suspendCancellableCoroutine { continuation ->
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data: NSData?, _: NSURLResponse?, error: NSError? ->
                when {
                    error != null -> continuation.resumeWithException(
                        IllegalStateException(error.localizedDescription)
                    )
                    data == null -> continuation.resumeWithException(IllegalStateException("Empty HTTP body"))
                    else -> {
                        val text: String? = NSString.create(data, NSUTF8StringEncoding)?.toString()
                        if (text == null) {
                            continuation.resumeWithException(IllegalStateException("Could not decode the HTTP body as UTF-8"))
                        } else {
                            continuation.resume(text)
                        }
                    }
                }
            }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }
    }

    private companion object {
        const val USER_AGENT: String = "INCIScan/1.0 (cosmetics ingredient scanner)"
    }
}
