package com.hnexperts.cosmetics.catalog.pipeline

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.catalog.data.CatalogWriter
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream

object CatalogSqlitePackager {
    fun writeGzip(outputDir: Path): Path {
        Files.createDirectories(outputDir)
        val sqlite: Path = outputDir.resolve("catalog.sqlite")
        Files.deleteIfExists(sqlite)
        val driver = JdbcSqliteDriver("jdbc:sqlite:${sqlite.toAbsolutePath()}")
        CatalogDatabase.Schema.create(driver)
        val database = CatalogDatabase(driver)
        CatalogSeeder(CatalogWriter(database)).seedIfEmpty()
        driver.close()
        val gzip: Path = outputDir.resolve("catalog.sqlite.gz")
        Files.newOutputStream(gzip).use { fileOut ->
            GZIPOutputStream(fileOut).use { compressed ->
                Files.copy(sqlite, compressed)
            }
        }
        return gzip
    }
}
