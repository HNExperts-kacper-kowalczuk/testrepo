package com.hnexperts.cosmetics.evaluation.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultA11yCountsTest {
    private val evaluateFormula: EvaluateFormula = EvaluationFactory.create()

    @Test
    fun countsProhibitedFindings() {
        val productInci: String = FixtureCatalog.products.first { item -> item.product.id == "problem-paste" }.product.inciRaw
        val assessment: ProductAssessment = evaluateFormula.evaluate(productInci, UserAvoidanceProfile.EMPTY)
        val counts: ResultA11yCounts = ResultA11yCounts.of(assessment)
        assertEquals(DangerLevel.PROHIBITED, assessment.overall)
        assertTrue(counts.prohibited >= 1)
        assertEquals(assessment.unknownCount, counts.unknown)
    }

    @Test
    fun countsHighFindingsForRinseOffMit() {
        val product = FixtureCatalog.products.first { item -> item.product.id == "strong-shampoo" }.product
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = product.inciRaw,
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.parse(product.usage)
        )
        val counts: ResultA11yCounts = ResultA11yCounts.of(assessment)
        assertTrue(counts.high >= 1)
    }

    @Test
    fun countsUnknownTokens() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, TotallyFakeIngredient, Glycerin",
            profile = UserAvoidanceProfile.EMPTY
        )
        val counts: ResultA11yCounts = ResultA11yCounts.of(assessment)
        assertTrue(counts.unknown >= 1)
        assertEquals(assessment.unknownCount, counts.unknown)
        assertEquals(0, counts.prohibited)
    }
}
