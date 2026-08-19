package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.pipeline.CatalogCommentValidator
import com.hnexperts.cosmetics.catalog.pipeline.CosingIngredientDump
import com.hnexperts.cosmetics.catalog.pipeline.CosingIngredientRecord
import com.hnexperts.cosmetics.catalog.pipeline.ObfProductDump
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Builds CosIng-scale catalog source dumps under catalog/ingest/.
 *
 * Output is a *candidate* dataset for review — the app still ships the
 * curated fixture catalog until the bundled-database bootstrap lands
 * (see docs/plan-catalog-ocr-ui.md, track B).
 */
object IngestCatalogSources {
    private val json: Json = Json { encodeDefaults = true }

    fun run(repoRoot: Path, skipCosing: Boolean, skipObf: Boolean, maxProducts: Int) {
        val outDir: Path = repoRoot.resolve("catalog/ingest")
        Files.createDirectories(outDir.resolve("cache"))
        val report = StringBuilder("# Catalog ingest report\n\nGenerated: ${Instant.now()}\n")
        if (!skipCosing) {
            ingestCosing(outDir, report)
        }
        if (!skipObf) {
            ingestObf(outDir, report, maxProducts)
        }
        outDir.resolve("report.md").writeText(report.toString())
        println(report)
        println("Wrote candidate dumps under $outDir")
    }

    private fun ingestCosing(outDir: Path, report: StringBuilder) {
        val client = EuropaSearchClient(IngestHttp())
        var segmentsDone = 0
        val metadata = client.fetchAllMetadata { _, _ ->
            segmentsDone++
            if (segmentsDone % 100 == 0) {
                println("CosIng enumeration: $segmentsDone segments done")
            }
        }
        val records: List<CosingIngredientRecord> = CosingAssembler().assemble(metadata)
        val dump = CosingIngredientDump(
            region = "EU",
            catalogVersion = "ingest-preview",
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = Instant.now().toString(),
            ingredients = records
        )
        val commentErrors: List<String> = CatalogCommentValidator().validate(dump)
        check(commentErrors.isEmpty()) {
            "Comment validation failed: ${commentErrors.take(5).joinToString("; ")}"
        }
        outDir.resolve("cosing-ingredients.json")
            .writeText(json.encodeToString(CosingIngredientDump.serializer(), dump))
        report.append("\n## CosIng ingredients\n")
        report.append("- inventory entries fetched: ${metadata.size}\n")
        report.append("- records written: ${records.size}\n")
        for ((level, count) in records.groupingBy { record -> record.dangerLevel }.eachCount()) {
            report.append("- $level: $count\n")
        }
    }

    private fun ingestObf(outDir: Path, report: StringBuilder, maxProducts: Int) {
        val cache: Path = outDir.resolve("cache/openbeautyfacts-products.jsonl.gz")
        if (!cache.exists()) {
            println("Downloading Open Beauty Facts dump…")
            Files.write(cache, IngestHttp().getBytes(OBF_DUMP_URL))
        }
        val result: ObfIngestResult = Files.newInputStream(cache).use { stream ->
            ObfProductsIngest(maxProducts).ingest(stream)
        }
        val dump = ObfProductDump(region = "EU", products = result.products)
        outDir.resolve("obf-products.json")
            .writeText(json.encodeToString(ObfProductDump.serializer(), dump))
        report.append("\n## Open Beauty Facts products\n")
        report.append("- dump rows read: ${result.totalRead}\n")
        report.append("- usable (GTIN + INCI): ${result.usable}\n")
        report.append("- tagged Poland: ${result.fromPoland}\n")
        report.append("- tagged EU/EEA: ${result.fromEu}\n")
        report.append("- written (max $maxProducts): ${result.products.size}\n")
    }

    /** Test hook: run the OBF mapping over an in-memory gzip. */
    fun ingestObfBytes(gzipped: ByteArray, maxProducts: Int): ObfIngestResult {
        return ObfProductsIngest(maxProducts).ingest(ByteArrayInputStream(gzipped))
    }

    private const val OBF_DUMP_URL: String =
        "https://static.openbeautyfacts.org/data/openbeautyfacts-products.jsonl.gz"
}

fun main(args: Array<String>) {
    val root: Path = Path.of(args.getOrNull(0) ?: error("Pass the repository root"))
    IngestCatalogSources.run(
        repoRoot = root,
        skipCosing = args.contains("--skip-cosing"),
        skipObf = args.contains("--skip-obf"),
        maxProducts = args.firstOrNull { arg -> arg.startsWith("--max-products=") }
            ?.substringAfter('=')?.toIntOrNull() ?: 20000
    )
}
