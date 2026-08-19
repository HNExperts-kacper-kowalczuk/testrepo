package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompareFormulasTest {
    private val evaluateFormula: EvaluateFormula = EvaluationFactory.create()

    @Test
    fun twoFixtureFormulasHaveDeterministicUniqueConcerns() {
        val paste = FixtureCatalog.products.first { item -> item.product.id == "problem-paste" }.product
        val cleanser = FixtureCatalog.products.first { item -> item.product.id == "gentle-cleanser" }.product
        val summary = CompareFormulas.fromAssessments(
            listOf(
                evaluateFormula.evaluate(paste.inciRaw, UserAvoidanceProfile.EMPTY, productName = paste.name),
                evaluateFormula.evaluate(cleanser.inciRaw, UserAvoidanceProfile.EMPTY, productName = cleanser.name)
            )
        )
        assertEquals(DangerLevel.PROHIBITED, summary.products[0].assessment.overall)
        assertEquals(DangerLevel.LOW, summary.products[1].assessment.overall)
        assertTrue(summary.uniqueHighOrProhibited.getValue(paste.name).contains("Formaldehyde"))
        assertTrue(summary.uniqueHighOrProhibited.getValue(cleanser.name).isEmpty())
        assertTrue(summary.sharedPersonalAvoids.isEmpty())
    }

    @Test
    fun fragranceFreeSharesParfumAcrossNightCreamAndMist() {
        val cream = FixtureCatalog.products.first { item -> item.product.id == "night-cream" }.product
        val mist = FixtureCatalog.products.first { item -> item.product.id == "fragrance-mist" }.product
        val profile: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(fragranceFree = true)
        val summary = CompareFormulas.fromAssessments(
            listOf(
                evaluateFormula.evaluate(cream.inciRaw, profile, productName = cream.name, usage = ProductUsage.LEAVE_ON),
                evaluateFormula.evaluate(mist.inciRaw, profile, productName = mist.name, usage = ProductUsage.LEAVE_ON)
            )
        )
        assertTrue(summary.sharedPersonalAvoids.contains("Parfum"))
    }
}

class FindLocalAlternativesTest {
    private val evaluateFormula: EvaluateFormula = EvaluationFactory.create()

    @Test
    fun suggestsSaferSameCategoryCandidate() {
        val nightCream = FixtureCatalog.products.first { item -> item.product.id == "night-cream" }.product
        val current = evaluateFormula.evaluate(
            inciRaw = nightCream.inciRaw,
            profile = UserAvoidanceProfile.EMPTY,
            productName = nightCream.name,
            usage = ProductUsage.LEAVE_ON
        ).copy(category = "moisturizer")
        val calmer: Product = Product(
            id = "calm-moisturizer",
            name = "Calm Cream",
            brand = "Fixture Lab",
            category = "moisturizer",
            inciRaw = "Aqua, Glycerin, Petrolatum",
            usage = "LEAVE_ON",
            source = "curated",
            verified = true
        )
        val hits = FindLocalAlternatives.invoke(
            current = current,
            candidates = listOf(nightCream, calmer),
            evaluateFormula = evaluateFormula,
            profile = UserAvoidanceProfile.EMPTY
        )
        assertEquals(1, hits.size)
        assertEquals("calm-moisturizer", hits.first().product.id)
        assertTrue(hits.first().assessment.overall != DangerLevel.RESTRICTED)
    }

    @Test
    fun skipsWhenCategoryMissing() {
        val current = evaluateFormula.evaluate("Aqua, Glycerin", UserAvoidanceProfile.EMPTY)
        val hits = FindLocalAlternatives.invoke(
            current = current,
            candidates = FixtureCatalog.products.map { item -> item.product },
            evaluateFormula = evaluateFormula,
            profile = UserAvoidanceProfile.EMPTY
        )
        assertTrue(hits.isEmpty())
    }
}

class ShareResultTextTest {
    @Test
    fun includesDisclaimerAndRatingWord() {
        val assessment = EvaluationFactory.create().evaluate(
            inciRaw = "Aqua, Glycerin",
            profile = UserAvoidanceProfile.EMPTY,
            productName = "Gentle Cream Cleanser"
        )
        val text: String = ShareResultText.format(assessment)
        assertTrue(text.contains("Gentle Cream Cleanser"))
        assertTrue(text.contains("SAFE") || text.contains("LOW"))
        assertTrue(text.contains(ShareResultText.DISCLAIMER))
    }
}
