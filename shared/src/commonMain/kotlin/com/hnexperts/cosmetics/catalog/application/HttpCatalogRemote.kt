package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CorruptCatalogException
import com.hnexperts.cosmetics.catalog.pipeline.CatalogManifestCodec
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import kotlinx.coroutines.CancellationException

class HttpCatalogRemote(
    private val http: SimpleHttpClient,
    private val baseUrl: String
) : CatalogRemote {
    override suspend fun publishedManifest(): CatalogManifest {
        val url: String = CatalogSyncPaths.manifestUrl(baseUrl)
        val text: String = when (val result: Outcome<String> = http.getText(url)) {
            is Outcome.Ok -> result.value
            is Outcome.Err -> throw IllegalStateException(result.failure.verboseMessage())
        }
        return decodeManifest(text)
    }

    private fun decodeManifest(text: String): CatalogManifest {
        return try {
            CatalogManifestCodec.decode(text)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (corrupt: CorruptCatalogException) {
            throw corrupt
        } catch (error: Exception) {
            throw CorruptCatalogException(error.message ?: "Could not decode catalog manifest")
        }
    }
}
