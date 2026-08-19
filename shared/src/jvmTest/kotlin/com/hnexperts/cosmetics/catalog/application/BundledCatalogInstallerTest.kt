package com.hnexperts.cosmetics.catalog.application

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.util.zip.GZIPOutputStream
import java.io.ByteArrayOutputStream

class BundledCatalogInstallerTest {
    @Test
    fun missingGzipDoesNothing() {
        val storage = MemoryStorage()
        BundledCatalogInstaller.install(storage, gzip = null, manifestJson = null)
        assertTrue(storage.written.isEmpty())
    }

    @Test
    fun inflatesAndWritesWhenChecksumIsNew() {
        val sqlite: ByteArray = "sqlite-bytes".encodeToByteArray()
        val gzip: ByteArray = ByteArrayOutputStream().use { out ->
            GZIPOutputStream(out).use { stream -> stream.write(sqlite) }
            out.toByteArray()
        }
        val storage = MemoryStorage()
        BundledCatalogInstaller.install(
            storage,
            gzip,
            """{"catalogVersion":"v","rulesetVersion":"r","builtAt":"t","region":"EU","checksum":"abc","productCount":1,"ingredientCount":1}"""
        )
        assertEquals("abc", storage.checksum)
        assertEquals("sqlite-bytes", storage.written.decodeToString())
    }

    private class MemoryStorage : CatalogFileStorage {
        var written: ByteArray = ByteArray(0)
        var checksum: String? = null

        override fun alreadyInstalled(checksum: String): Boolean {
            return this.checksum == checksum && written.isNotEmpty()
        }

        override fun writeCatalog(sqliteBytes: ByteArray, checksum: String) {
            written = sqliteBytes
            this.checksum = checksum
        }
    }
}
