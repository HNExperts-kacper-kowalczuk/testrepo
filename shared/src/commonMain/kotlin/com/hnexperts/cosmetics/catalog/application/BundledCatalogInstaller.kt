package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.GzipCodec
import com.hnexperts.cosmetics.catalog.pipeline.CatalogManifestCodec

interface CatalogFileStorage {
    fun alreadyInstalled(checksum: String): Boolean
    fun writeCatalog(sqliteBytes: ByteArray, checksum: String)
}

object BundledCatalogInstaller {
    fun install(storage: CatalogFileStorage, gzip: ByteArray?, manifestJson: String?) {
        if (gzip == null || gzip.isEmpty()) {
            return
        }
        val checksum: String = checksumOf(manifestJson)
        if (storage.alreadyInstalled(checksum)) {
            return
        }
        storage.writeCatalog(GzipCodec.inflate(gzip), checksum)
    }

    private fun checksumOf(manifestJson: String?): String {
        if (manifestJson.isNullOrBlank()) {
            return "unknown"
        }
        return CatalogManifestCodec.decode(manifestJson).checksum
    }
}
