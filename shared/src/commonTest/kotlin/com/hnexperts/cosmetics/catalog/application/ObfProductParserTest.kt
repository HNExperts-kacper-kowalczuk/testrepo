package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ObfProductParserTest {
    @Test
    fun mapsACompleteBeautyFactsProduct() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "5901234123457",
            body = """
                {"status":1,"product":{
                  "product_name":"Nawilżający szampon",
                  "brands":"Ziaja, Ziaja Ltd",
                  "categories_tags":["en:shampoos"],
                  "ingredients_text":"Aqua, Sodium Laureth Sulfate, Cocamidopropyl Betaine, Parfum"
                }}
            """.trimIndent()
        )
        val found = assertIs<OnlineGtinHit.WithIngredients>(hit)
        assertEquals("Nawilżający szampon", found.name)
        assertEquals("Ziaja", found.brand)
        assertEquals(ProductUsage.RINSE_OFF, found.usage)
        assertTrue(found.inciRaw.startsWith("Aqua"))
    }

    @Test
    fun missingIngredientListIsADistinctMiss() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "4000000000001",
            body = """{"status":1,"product":{"product_name":"Mystery cream"}}"""
        )
        val miss = assertIs<OnlineGtinHit.MissingIngredients>(hit)
        assertEquals("Mystery cream", miss.name)
    }

    @Test
    fun unknownStatusIsNotFound() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "000",
            body = """{"status":0,"status_verbose":"product not found"}"""
        )
        assertIs<OnlineGtinHit.NotFound>(hit)
    }

    @Test
    fun prefersPolishIngredientTextForGs1PolandGtins() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "5901887019367",
            body = """{"status":1,"product":{
              "product_name":"Cream EN",
              "product_name_pl":"Anty-perspirant w kremie",
              "ingredients_text_en":"Aqua, Glycerin, Niacinamide, Panthenol, Extra English filler",
              "ingredients_text_pl":"Aqua, Aluminum Chlorohydrate, Dimethicone, Steareth-2"
            }}"""
        )
        val found = assertIs<OnlineGtinHit.WithIngredients>(hit)
        assertEquals("Anty-perspirant w kremie", found.name)
        assertEquals("Aqua, Aluminum Chlorohydrate, Dimethicone, Steareth-2", found.inciRaw)
    }

    @Test
    fun usesStructuredIngredientsWhenTextFieldsAreMissing() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "5901234123457",
            body = """{"status":1,"product":{
              "product_name_pl":"Krem",
              "ingredients":[
                {"text":"Aqua"},
                {"id":"en:glycerin"},
                {"text":"Panthenol"},
                {"text":"Niacinamide"}
              ]
            }}"""
        )
        val found = assertIs<OnlineGtinHit.WithIngredients>(hit)
        assertEquals("Aqua, glycerin, Panthenol, Niacinamide", found.inciRaw)
    }

    @Test
    fun prefersEnglishIngredientText() {
        val hit: OnlineGtinHit = ObfProductParser.parse(
            gtin = "1",
            body = """{"status":1,"product":{
              "product_name":"Cream",
              "ingredients_text":"short",
              "ingredients_text_en":"Aqua, Glycerin, Niacinamide, Panthenol"
            }}"""
        )
        val found = assertIs<OnlineGtinHit.WithIngredients>(hit)
        assertEquals("Aqua, Glycerin, Niacinamide, Panthenol", found.inciRaw)
    }
}
