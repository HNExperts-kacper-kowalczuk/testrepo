package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

object ExportCatalogSources {
    fun write(repoRoot: Path) {
        val sources: Path = repoRoot.resolve("catalog/sources")
        Files.createDirectories(sources)
        val ingredientsDump = CatalogSourceCodec.parseIngredients(CatalogSourceCodec.encodeIngredients())
        val productsDump = CatalogSourceCodec.parseProducts(CatalogSourceCodec.encodeProducts())
        val build = CatalogBuilder.build(ingredientsDump, productsDump)
        sources.resolve("cosing-ingredients.json").writeText(CatalogSourceCodec.encodeIngredients())
        sources.resolve("obf-products.json").writeText(CatalogSourceCodec.encodeProducts())
        val manifestJson: String = CatalogManifestCodec.encode(build.manifest)
        sources.resolve("catalog-manifest.json").writeText(manifestJson)
        val bundled: Path = repoRoot.resolve(
            "shared/src/commonMain/composeResources/files/catalog-manifest.json"
        )
        Files.createDirectories(bundled.parent)
        bundled.writeText(manifestJson)
        CatalogSqlitePackager.writeGzip(repoRoot.resolve("catalog/build"))
        check(build.manifest.checksum == CatalogIntegrity.fixtureChecksum()) {
            "exported checksum ${build.manifest.checksum} != fixture ${CatalogIntegrity.fixtureChecksum()}"
        }
    }
}

fun main(args: Array<String>) {
    val root: Path = Path.of(args.getOrNull(0) ?: error("Pass the repository root"))
    ExportCatalogSources.write(root)
    println("Wrote catalog sources under $root/catalog/sources")
}
