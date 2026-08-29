package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class IosCatalogStorage : CatalogFileStorage {
    override fun alreadyInstalled(checksum: String): Boolean {
        val dest: String = catalogPath()
        val stamp: String = stampPath()
        val files: NSFileManager = NSFileManager.defaultManager
        if (!files.fileExistsAtPath(dest) || !files.fileExistsAtPath(stamp)) {
            return false
        }
        val stored: String = files.contentsAtPath(stamp)?.let { data ->
            bytesToString(data)
        } ?: return false
        return stored == checksum
    }

    override fun writeCatalog(sqliteBytes: ByteArray, checksum: String) {
        writeBytes(catalogPath(), sqliteBytes)
        writeBytes(stampPath(), checksum.encodeToByteArray())
    }

    fun catalogDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        return paths.first() as String
    }

    private fun catalogPath(): String {
        return "${catalogDirectory()}/catalog.db"
    }

    private fun stampPath(): String {
        return "${catalogDirectory()}/catalog.bundle.checksum"
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun writeBytes(path: String, bytes: ByteArray) {
        bytes.usePinned { pinned ->
            val data: NSData = NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            data.writeToFile(path, atomically = true)
        }
    }

    private fun bytesToString(data: NSData): String {
        val bytes: ByteArray = ByteArray(data.length.toInt())
        if (bytes.isEmpty()) {
            return ""
        }
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        return bytes.decodeToString()
    }
}

object IosBundledCatalog {
    @OptIn(ExperimentalResourceApi::class)
    fun install(): String {
        val storage = IosCatalogStorage()
        val gzip: ByteArray? = readResource("files/catalog.sqlite.gz")
        val manifest: String? = readResource("files/catalog-manifest.json")?.decodeToString()
        BundledCatalogInstaller.install(storage, gzip, manifest)
        return storage.catalogDirectory()
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun readResource(path: String): ByteArray? {
        return try {
            runBlocking { Res.readBytes(path) }
        } catch (missing: Exception) {
            null
        }
    }
}
