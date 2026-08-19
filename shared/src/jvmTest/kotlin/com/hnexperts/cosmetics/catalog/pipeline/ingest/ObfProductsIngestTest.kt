package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.pipeline.ObfProductRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ObfProductsIngestTest {
    private val ingest = ObfProductsIngest(maxProducts = 10)

    private fun product(rawJson: String) = Json.parseToJsonElement(rawJson).jsonObject

    @Test
    fun mapsUsableProduct() {
        val record: ObfProductRecord? = ingest.toRecord(
            product(
                """
                {"code":"5901234123457",
                 "product_name":"Nawilżający szampon",
                 "brands":"Ziaja, Ziaja Ltd",
                 "categories":"Shampoos, Hair care",
                 "categories_tags":["en:shampoos"],
                 "countries_tags":["en:poland"],
                 "ingredients_text":"Aqua, Sodium Laureth Sulfate, Cocamidopropyl Betaine"}
                """
            )
        )
        assertNotNull(record)
        assertEquals("obf-5901234123457", record.id)
        assertEquals("Ziaja", record.brand)
        assertEquals("RINSE_OFF", record.usage)
        assertEquals(listOf("5901234123457"), record.gtins)
    }

    @Test
    fun productWithoutInciIsSkipped() {
        val record = ingest.toRecord(
            product("""{"code":"5901234123457","product_name":"Krem","ingredients_text":""}""")
        )
        assertNull(record)
    }

    @Test
    fun invalidGtinIsSkipped() {
        val record = ingest.toRecord(
            product("""{"code":"abc","product_name":"Krem","ingredients_text":"Aqua, Glycerin, Panthenol"}""")
        )
        assertNull(record)
    }

    @Test
    fun localizedInciIsUsedWhenEnglishMissing() {
        val record = ingest.toRecord(
            product(
                """
                {"code":"5901234123457","product_name":"Balsam",
                 "ingredients_text_pl":"Aqua, Glycerin, Butyrospermum Parkii Butter"}
                """
            )
        )
        assertNotNull(record)
        assertEquals("Aqua, Glycerin, Butyrospermum Parkii Butter", record.inciRaw)
    }

    @Test
    fun polishProductsAreSelectedFirstUnderTheCap(): Unit {
        val lines: String = listOf(
            """{"code":"40000000000001","product_name":"DE","countries_tags":["en:germany"],"ingredients_text":"Aqua, Glycerin, Panthenol"}""",
            """{"code":"59000000000001","product_name":"PL","countries_tags":["en:poland"],"ingredients_text":"Aqua, Glycerin, Urea"}""",
            """{"code":"00000000000001","product_name":"US","countries_tags":["en:united-states"],"ingredients_text":"Aqua, Glycerin, Niacinamide"}"""
        ).joinToString(separator = "\n")
        val gzipped: ByteArray = gzip(lines)
        val result: ObfIngestResult = ObfProductsIngest(maxProducts = 2)
            .ingest(gzipped.inputStream())
        assertEquals(listOf("PL", "DE"), result.products.map { record -> record.name })
        assertEquals(3, result.usable)
        assertEquals(1, result.fromPoland)
    }

    private fun gzip(text: String): ByteArray {
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(bytes).use { stream -> stream.write(text.encodeToByteArray()) }
        return bytes.toByteArray()
    }
}
