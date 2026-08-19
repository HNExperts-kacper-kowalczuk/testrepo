package com.hnexperts.cosmetics.catalog.pipeline

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Reads catalog/ingest (or falls back to catalog/sources) and writes the
 * bundled sqlite gzip plus manifest into compose resources.
 */
object PackShippedCatalog {
    fun write(repoRoot: Path, maxProducts: Int = Int.MAX_VALUE): PackedCatalog {
        val ingredients: CosingIngredientDump = loadIngredients(repoRoot)
        val products: ObfProductDump = loadProducts(repoRoot)
        var cap: Int = maxProducts
        var build: CatalogBuild = merge(ingredients, products, cap)
        var gzip: Path = CatalogSqlitePackager.writeFromBuild(repoRoot.resolve("catalog/build"), build)
        while (Files.size(gzip) > MAX_GZIP_BYTES && cap > MIN_PRODUCTS) {
            cap = (build.products.size / 2).coerceAtLeast(MIN_PRODUCTS)
            build = merge(ingredients, products, cap)
            gzip = CatalogSqlitePackager.writeFromBuild(repoRoot.resolve("catalog/build"), build)
        }
        val manifestJson: String = CatalogManifestCodec.encode(build.manifest)
        val bundledDir: Path = repoRoot.resolve("shared/src/commonMain/composeResources/files")
        Files.createDirectories(bundledDir)
        bundledDir.resolve("catalog-manifest.json").writeText(manifestJson)
        val bundledGzip: Path = bundledDir.resolve("catalog.sqlite.gz")
        Files.deleteIfExists(bundledGzip)
        Files.copy(gzip, bundledGzip)
        return PackedCatalog(build = build, gzip = bundledGzip)
    }

    private fun merge(
        ingredients: CosingIngredientDump,
        products: ObfProductDump,
        maxProducts: Int
    ): CatalogBuild {
        return ShippedCatalogMerger.merge(
            ingestedIngredients = ingredients,
            ingestedProducts = products,
            maxProducts = maxProducts,
            builtAt = ingredients.builtAt
        )
    }

    private fun loadIngredients(repoRoot: Path): CosingIngredientDump {
        val ingest: Path = repoRoot.resolve("catalog/ingest/cosing-ingredients.json")
        val sources: Path = repoRoot.resolve("catalog/sources/cosing-ingredients.json")
        val raw: String = (if (ingest.exists()) ingest else sources).readText()
        return CatalogSourceCodec.parseIngredients(raw)
    }

    private fun loadProducts(repoRoot: Path): ObfProductDump {
        val ingest: Path = repoRoot.resolve("catalog/ingest/obf-products.json")
        val sources: Path = repoRoot.resolve("catalog/sources/obf-products.json")
        val raw: String = (if (ingest.exists()) ingest else sources).readText()
        return CatalogSourceCodec.parseProducts(raw)
    }

    private const val MAX_GZIP_BYTES: Long = 20L * 1024L * 1024L
    private const val MIN_PRODUCTS: Int = 8
}

data class PackedCatalog(
    val build: CatalogBuild,
    val gzip: Path
)

fun main(args: Array<String>) {
    val root: Path = Path.of(args.getOrNull(0) ?: error("Pass the repository root"))
    val maxProducts: Int = args.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE
    val packed: PackedCatalog = PackShippedCatalog.write(root, maxProducts)
    val sizeMb: String = "%.1f".format(Files.size(packed.gzip) / (1024.0 * 1024.0))
    println(
        "Packed catalog ${packed.build.manifest.catalogVersion}: " +
            "${packed.build.manifest.ingredientCount} ingredients, " +
            "${packed.build.manifest.productCount} products, ${sizeMb} MB gzip"
    )
}
