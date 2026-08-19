package com.hnexperts.cosmetics.scanning.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IngredientBlockExtractorTest {
    @Test
    fun keepsOnlyTheBlockAfterEnglishIngredientsHeader() {
        val transcript: String = """
            NIVEA Soft Moisturizing Cream
            For face, body and hands
            Ingredients: Aqua, Glycerin, Paraffinum Liquidum,
            Myristyl Alcohol, Parfum, Limonene
            Directions: apply daily to clean skin.
            Made in Germany
        """.trimIndent()
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertTrue(extracted.startsWith("Aqua, Glycerin"))
        assertTrue(extracted.contains("Limonene"))
        assertFalse(extracted.contains("Directions", ignoreCase = true))
        assertFalse(extracted.contains("Moisturizing Cream"))
    }

    @Test
    fun keepsOnlyTheBlockAfterPolishSkladHeader() {
        val transcript: String = """
            Szampon do włosów normalnych
            Skład: Aqua, Sodium Laureth Sulfate, Cocamidopropyl Betaine,
            Parfum, Sodium Benzoate
            Sposób użycia: nanieść na mokre włosy, spłukać.
            Wyprodukowano w Polsce
        """.trimIndent()
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertTrue(extracted.startsWith("Aqua, Sodium Laureth Sulfate"))
        assertFalse(extracted.contains("Sposób", ignoreCase = true))
        assertFalse(extracted.contains("Szampon"))
    }

    @Test
    fun toleratesOcrReadingSkladWithoutDiacritics() {
        val transcript: String = "Sklad: Aqua, Glycerin\nOstrzezenia: unikac kontaktu z oczami"
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin", extracted)
    }

    @Test
    fun usesTheLastHeaderWhenBrandingRepeatsTheWord() {
        val transcript: String = """
            Simple ingredients, honest care
            INGREDIENTS: Aqua, Niacinamide, Glycerin
        """.trimIndent()
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Niacinamide, Glycerin", extracted)
    }

    @Test
    fun bilingualSlashHeaderIsConsumedEntirely() {
        val transcript: String = "Ingredients/Składniki: Aqua, Glycerin, Panthenol"
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin, Panthenol", extracted)
    }

    @Test
    fun headerWithInciInParenthesesIsConsumed() {
        val transcript: String = "INGREDIENTS (INCI): Aqua, Glycerin"
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin", extracted)
    }

    @Test
    fun tightCropWithoutHeaderIsUsedAsIs() {
        val transcript: String = "Aqua, Glycerin, Niacinamide,\nSodium Hyaluronate"
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin, Niacinamide,\nSodium Hyaluronate", extracted)
    }

    @Test
    fun junkLinesAreDropped() {
        val transcript: String = """
            Ingredients: Aqua, Glycerin
            5901234123457
            50 ml
            Parfum, Limonene
        """.trimIndent()
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin\nParfum, Limonene", extracted)
    }

    @Test
    fun inlineStopAfterSentenceEndCutsTheLine() {
        val transcript: String = "Skład: Aqua, Alcohol Denat., Parfum. Sposób użycia: rozpylić."
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Alcohol Denat., Parfum", extracted.trimEnd('.'))
    }

    @Test
    fun germanHeaderAndStopAreRecognized() {
        val transcript: String = """
            Inhaltsstoffe: Aqua, Glycerin, Urea
            Anwendung: täglich auftragen
        """.trimIndent()
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin, Urea", extracted)
    }

    @Test
    fun emptyInputStaysEmpty() {
        assertEquals("", IngredientBlockExtractor.extract("   \n  "))
    }

    @Test
    fun stopSectionWithoutHeaderStillCuts() {
        val transcript: String = "Aqua, Glycerin, Parfum\nHow to use: apply evenly"
        val extracted: String = IngredientBlockExtractor.extract(transcript)
        assertEquals("Aqua, Glycerin, Parfum", extracted)
    }
}
