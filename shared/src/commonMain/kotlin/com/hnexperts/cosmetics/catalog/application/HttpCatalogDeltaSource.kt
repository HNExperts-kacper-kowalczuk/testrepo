package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.GzipCodec
import com.hnexperts.cosmetics.catalog.domain.CorruptCatalogException
import com.hnexperts.cosmetics.catalog.pipeline.CatalogSourceCodec
import com.hnexperts.cosmetics.crypto.Sha256
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import kotlinx.coroutines.CancellationException

class HttpCatalogDeltaSource(
    private val http: SimpleHttpClient,
    private val baseUrl: String
) : CatalogDeltaSource {
    override suspend fun deltaFor(fromVersion: String, published: CatalogManifest): CatalogDelta? {
        val url: String = CatalogSyncPaths.deltaUrl(baseUrl, fromVersion, published.catalogVersion)
        val bytes: ByteArray = readPayload(url)
        verifySize(bytes)
        verifyChecksum(bytes, published.checksum)
        return decodeDelta(bytes)
    }

    private suspend fun readPayload(url: String): ByteArray {
        return when (val result: Outcome<ByteArray> = http.getBytes(url)) {
            is Outcome.Ok -> result.value
            is Outcome.Err -> throw IllegalStateException(result.failure.verboseMessage())
        }
    }

    private fun verifySize(bytes: ByteArray) {
        if (bytes.size > CatalogSyncPaths.MAX_BYTES) {
            throw CorruptCatalogException("Catalog payload exceeds ${CatalogSyncPaths.MAX_BYTES} bytes")
        }
    }

    private fun verifyChecksum(bytes: ByteArray, expected: String) {
        val digest: String = Sha256.hex(bytes)
        if (!digest.equals(expected, ignoreCase = true)) {
            throw CorruptCatalogException("Catalog payload checksum mismatch")
        }
    }

    private fun decodeDelta(bytes: ByteArray): CatalogDelta {
        return try {
            val json: String = GzipCodec.inflate(bytes).decodeToString()
            CatalogSourceCodec.decodeDelta(json)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (corrupt: CorruptCatalogException) {
            throw corrupt
        } catch (error: Exception) {
            throw CorruptCatalogException(error.message ?: "Could not decode catalog delta")
        }
    }
}
