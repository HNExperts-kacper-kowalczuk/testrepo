package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome

interface SimpleHttpClient {
    suspend fun getText(url: String): Outcome<String>

    suspend fun getBytes(url: String): Outcome<ByteArray> {
        return Outcome.Err(
            AppFailure.Network(operation = "http.get.bytes", detail = "Binary GET is not implemented")
        )
    }

    suspend fun postJson(url: String, body: String): Outcome<Unit> {
        return Outcome.Err(
            AppFailure.Network(operation = "http.post", detail = "JSON POST is not implemented")
        )
    }
}
