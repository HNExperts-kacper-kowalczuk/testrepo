package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.pipeline.ObfProductRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream

data class ObfIngestResult(
    val products: List<ObfProductRecord>,
    val totalRead: Int,
    val usable: Int,
    val fromPoland: Int,
    val fromEu: Int
)

/**
 * Streams the Open Beauty Facts JSONL dump and keeps products that have a
 * valid GTIN and a usable INCI text. Products sold in Poland come first,
 * then the rest of the EU/EEA, then everything else, up to [maxProducts].
 */
class ObfProductsIngest(
    private val maxProducts: Int = DEFAULT_MAX_PRODUCTS
) {
    private val json: Json = Json { ignoreUnknownKeys = true }

    fun ingest(gzippedJsonl: InputStream): ObfIngestResult {
        val buckets: Map<Int, MutableList<ObfProductRecord>> =
            mapOf(0 to mutableListOf(), 1 to mutableListOf(), 2 to mutableListOf())
        var total = 0
        forEachLine(gzippedJsonl) { line ->
            total++
            val product: JsonObject = parseLine(line) ?: return@forEachLine
            val record: ObfProductRecord = toRecord(product) ?: return@forEachLine
            buckets.getValue(priority(product)).add(record)
        }
        val poland: Int = buckets.getValue(0).size
        val eu: Int = buckets.getValue(1).size
        val usable: Int = buckets.values.sumOf { bucket -> bucket.size }
        val selected: List<ObfProductRecord> = buckets.toSortedMap().values
            .flatten()
            .take(maxProducts)
        return ObfIngestResult(
            products = selected,
            totalRead = total,
            usable = usable,
            fromPoland = poland,
            fromEu = eu + poland
        )
    }

    fun toRecord(product: JsonObject): ObfProductRecord? {
        val code: String = product.stringField("code") ?: return null
        if (!GTIN.matches(code)) {
            return null
        }
        val inci: String = bestInciText(product) ?: return null
        val name: String = product.stringField("product_name")
            ?: product.stringField("generic_name")
            ?: return null
        return ObfProductRecord(
            id = "obf-$code",
            name = name,
            brand = product.stringField("brands")?.split(',')?.first()?.trim(),
            category = product.stringField("categories")?.split(',')?.first()?.trim(),
            inciRaw = inci,
            usage = usageFor(product),
            source = "obf",
            verified = false,
            gtins = listOf(code)
        )
    }

    /** Prefer the English INCI text, then the longest non-blank variant. */
    private fun bestInciText(product: JsonObject): String? {
        val english: String? = product.stringField("ingredients_text_en")
            ?: product.stringField("ingredients_text")
        if (english != null && english.length >= MIN_INCI_LENGTH) {
            return english
        }
        return product.entries
            .filter { entry -> entry.key.startsWith("ingredients_text") }
            .mapNotNull { entry -> product.stringField(entry.key) }
            .filter { text -> text.length >= MIN_INCI_LENGTH }
            .maxByOrNull { text -> text.length }
    }

    private fun usageFor(product: JsonObject): String? {
        val categories: String = (product.tags("categories_tags") +
            listOfNotNull(product.stringField("categories"))).joinToString(" ").lowercase()
        return when {
            RINSE_OFF.containsMatchIn(categories) -> "RINSE_OFF"
            SPRAY.containsMatchIn(categories) -> "SPRAY"
            LIP.containsMatchIn(categories) -> "LIP"
            EYE.containsMatchIn(categories) -> "EYE"
            categories.isNotBlank() -> "LEAVE_ON"
            else -> null
        }
    }

    private fun priority(product: JsonObject): Int {
        val countries: List<String> = product.tags("countries_tags").map { tag -> tag.substringAfter(':') }
        return when {
            countries.contains("poland") -> 0
            countries.any { country -> EU_COUNTRIES.contains(country) } -> 1
            else -> 2
        }
    }

    private fun parseLine(line: String): JsonObject? {
        if (line.isBlank()) {
            return null
        }
        return try {
            json.parseToJsonElement(line).jsonObject
        } catch (invalid: Exception) {
            null
        }
    }

    private fun forEachLine(gzipped: InputStream, block: (String) -> Unit) {
        val reader = BufferedReader(InputStreamReader(GZIPInputStream(gzipped), StandardCharsets.UTF_8))
        reader.useLines { lines -> lines.forEach(block) }
    }

    private fun JsonObject.stringField(key: String): String? {
        val element = this[key] ?: return null
        val value: String = try {
            element.jsonPrimitive.content.trim()
        } catch (notPrimitive: Exception) {
            return null
        }
        return value.ifEmpty { null }
    }

    private fun JsonObject.tags(key: String): List<String> {
        val element = this[key] ?: return emptyList()
        return try {
            element.jsonArray.map { tag -> tag.jsonPrimitive.content }
        } catch (notArray: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val DEFAULT_MAX_PRODUCTS: Int = 20000
        const val MIN_INCI_LENGTH: Int = 20
        val GTIN: Regex = Regex("\\d{8,14}")
        val RINSE_OFF: Regex = Regex("shampoo|shower|soap|bath|cleanser|wash|rinse|conditioner|scrub|peeling")
        val SPRAY: Regex = Regex("deodorant|antiperspirant|perfume|eau-de|mist|spray|hairspray")
        val LIP: Regex = Regex("lipstick|lip-|lips\\b|balm")
        val EYE: Regex = Regex("mascara|eyeliner|eye-shadow|eyeshadow|eye-cream")
        val EU_COUNTRIES: Set<String> = setOf(
            "austria", "belgium", "bulgaria", "croatia", "cyprus", "czech-republic", "czechia",
            "denmark", "estonia", "finland", "france", "germany", "greece", "hungary", "ireland",
            "italy", "latvia", "lithuania", "luxembourg", "malta", "netherlands", "poland",
            "portugal", "romania", "slovakia", "slovenia", "spain", "sweden",
            "norway", "switzerland", "iceland", "liechtenstein", "european-union", "united-kingdom"
        )
    }
}
