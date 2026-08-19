package com.hnexperts.cosmetics.network

import com.hnexperts.cosmetics.failure.Outcome

interface SimpleHttpClient {
    suspend fun getText(url: String): Outcome<String>
}
