package com.hnexperts.cosmetics.catalog.pipeline

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.catalog.data.CatalogWriter
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream

object CatalogSqlitePackager {
    fun writeGzip(outputDir: Path): Path {
        return writeFromBuild(outputDir, fixtureBuild())
    }

    fun writeFromBuild(outputDir: Path, build: CatalogBuild): Path {
        Files.createDirectories(outputDir)
        val sqlite: Path = outputDir.resolve("catalog.sqlite")
        Files.deleteIfExists(sqlite)
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${sqlite.toAbsolutePath()}")
        CatalogDatabase.Schema.create(driver)
        stampSchemaVersion(driver)
        val database = CatalogDatabase(driver)
        CatalogWriter(database).replaceAll(build.ingredients, build.products, build.meta)
        driver.close()
        val gzip: Path = outputDir.resolve("catalog.sqlite.gz")
        Files.deleteIfExists(gzip)
        Files.newOutputStream(gzip).use { fileOut ->
            GZIPOutputStream(fileOut).use { compressed ->
                Files.copy(sqlite, compressed)
            }
        }
        return gzip
    }

    private fun stampSchemaVersion(driver: SqlDriver) {
        val version: Long = CatalogDatabase.Schema.version
        driver.execute(
            identifier = null,
            sql = "PRAGMA user_version = $version",
            parameters = 0
        )
    }

    private fun fixtureBuild(): CatalogBuild {
        val ingredientsDump = CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        val productsDump = CatalogSourceCodec.parseProducts(CatalogSourceCodec.encodeProducts())
        return CatalogBuilder.build(ingredientsDump, productsDump)
    }
}
