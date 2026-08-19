package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.application.CatalogManifest
import kotlinx.serialization.json.Json

object CatalogManifestCodec {
    private val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(manifest: CatalogManifest): String {
        return json.encodeToString(CatalogManifest.serializer(), manifest)
    }

    fun decode(raw: String): CatalogManifest {
        return json.decodeFromString(CatalogManifest.serializer(), raw)
    }
}
