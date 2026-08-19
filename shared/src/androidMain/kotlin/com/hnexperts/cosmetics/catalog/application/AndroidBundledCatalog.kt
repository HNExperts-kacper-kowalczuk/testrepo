package com.hnexperts.cosmetics.catalog.application

import android.content.Context
import com.hnexperts.cosmetics.resources.Res
import java.io.File
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi

class AndroidCatalogStorage(
    private val context: Context
) : CatalogFileStorage {
    override fun alreadyInstalled(checksum: String): Boolean {
        val dest: File = context.getDatabasePath(DB_NAME)
        val stamp: File = stampFile()
        return dest.exists() && dest.length() > 0L && stamp.exists() && stamp.readText() == checksum
    }

    override fun writeCatalog(sqliteBytes: ByteArray, checksum: String) {
        val dest: File = context.getDatabasePath(DB_NAME)
        dest.parentFile?.mkdirs()
        dest.writeBytes(sqliteBytes)
        stampFile().writeText(checksum)
    }

    private fun stampFile(): File {
        return File(context.filesDir, STAMP_NAME)
    }

    private companion object {
        const val DB_NAME: String = "catalog.db"
        const val STAMP_NAME: String = "catalog.bundle.checksum"
    }
}

object AndroidBundledCatalog {
    @OptIn(ExperimentalResourceApi::class)
    fun install(context: Context) {
        val gzip: ByteArray? = readResource("files/catalog.sqlite.gz")
        val manifest: String? = readResource("files/catalog-manifest.json")?.decodeToString()
        BundledCatalogInstaller.install(AndroidCatalogStorage(context), gzip, manifest)
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
