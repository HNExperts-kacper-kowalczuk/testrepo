package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.pipeline.CatalogManifestCodec
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class HttpCatalogRemoteTest {
    @Test
    fun readsManifestFromConfiguredBaseUrl() = runBlocking {
        val published = CatalogManifest(
            catalogVersion = "2026.09-hosted",
            rulesetVersion = "2026.08-fixture",
            builtAt = "2026-08-28T00:00:00Z",
            region = "EU",
            checksum = "abc123",
            productCount = 9,
            ingredientCount = 36
        )
        val base: String = "https://catalog.example.test"
        val http = MapHttp(
            textBodies = mapOf(CatalogSyncPaths.manifestUrl(base) to CatalogManifestCodec.encode(published))
        )
        val remote = HttpCatalogRemote(http, base)
        val loaded: CatalogManifest = remote.publishedManifest()
        assertEquals("2026.09-hosted", loaded.catalogVersion)
        assertEquals("abc123", loaded.checksum)
        assertEquals(9, loaded.productCount)
    }

    private class MapHttp(
        private val textBodies: Map<String, String>
    ) : SimpleHttpClient {
        override suspend fun getText(url: String): Outcome<String> {
            val body: String = textBodies[url]
                ?: return Outcome.Err(AppFailure.Network(operation = "http.get", detail = "missing $url"))
            return Outcome.Ok(body)
        }
    }
}
