package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluateFormulaTest {
    private val evaluateFormula: EvaluateFormula = EvaluationFactory.create()

    @Test
    fun gentleCleanserIsLowOverall() {
        val productInci: String = FixtureCatalog.products.first { item -> item.product.id == "gentle-cleanser" }.product.inciRaw
        val assessment: ProductAssessment = evaluateFormula.evaluate(productInci, UserAvoidanceProfile.EMPTY)
        assertEquals(DangerLevel.LOW, assessment.overall)
        assertEquals(0, assessment.unknownCount)
        assertTrue(assessment.suitableForUser)
    }

    @Test
    fun shampooWithMitIsHighWhenRinseOff() {
        val product = FixtureCatalog.products.first { item -> item.product.id == "strong-shampoo" }.product
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = product.inciRaw,
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.parse(product.usage)
        )
        assertEquals(DangerLevel.HIGH, assessment.overall)
    }

    @Test
    fun mitInLeaveOnIsProhibited() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Methylisothiazolinone, Glycerin",
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.LEAVE_ON
        )
        assertEquals(DangerLevel.PROHIBITED, assessment.overall)
        assertTrue(assessment.findings.first { finding -> finding.ingredient.id == "methylisothiazolinone" }.usageAdjusted)
    }

    @Test
    fun prohibitedIngredientDrivesAvoidRating() {
        val productInci: String = FixtureCatalog.products.first { item -> item.product.id == "problem-paste" }.product.inciRaw
        val assessment: ProductAssessment = evaluateFormula.evaluate(productInci, UserAvoidanceProfile.EMPTY)
        assertEquals(DangerLevel.PROHIBITED, assessment.overall)
    }

    @Test
    fun unknownTokensDoNotCountAsSafe() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, TotallyFakeIngredient, Glycerin",
            profile = UserAvoidanceProfile.EMPTY
        )
        assertEquals(DangerLevel.SAFE, assessment.overall)
        assertTrue(assessment.unknownCount >= 1)
        assertTrue(assessment.findings.any { finding -> finding.level == DangerLevel.UNKNOWN })
    }

    @Test
    fun fragranceFreeMarksParfumAsPersonalAvoid() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Glycerin, Parfum",
            profile = UserAvoidanceProfile(
                pregnancyCaution = false,
                fragranceFree = true,
                avoidedIngredientIds = emptySet()
            )
        )
        assertFalse(assessment.suitableForUser)
        assertTrue(assessment.findings.first { finding -> finding.ingredient.id == "parfum" }.personalAvoid)
    }

    @Test
    fun pregnancyCautionFlagsRetinol() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Retinol, Glycerin",
            profile = UserAvoidanceProfile(
                pregnancyCaution = true,
                fragranceFree = false,
                avoidedIngredientIds = emptySet()
            )
        )
        assertFalse(assessment.suitableForUser)
        assertEquals(DangerLevel.RESTRICTED, assessment.overall)
    }

    @Test
    fun asyncEvaluationMatchesSynchronousPath() {
        val productInci: String = FixtureCatalog.products
            .joinToString(", ") { item -> item.product.inciRaw }
        val sequential: ProductAssessment = evaluateFormula.evaluate(productInci, UserAvoidanceProfile.EMPTY)
        val concurrent: ProductAssessment = kotlinx.coroutines.runBlocking {
            evaluateFormula.evaluateAsync(productInci, UserAvoidanceProfile.EMPTY)
        }
        assertEquals(sequential.overall, concurrent.overall)
        assertEquals(sequential.findings.map { finding -> finding.ingredient.id }, concurrent.findings.map { finding -> finding.ingredient.id })
        assertEquals(sequential.unknownCount, concurrent.unknownCount)
    }

    @Test
    fun explicitAvoidListOverridesOtherwiseSafeFormula() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Glycerin",
            profile = UserAvoidanceProfile(
                pregnancyCaution = false,
                fragranceFree = false,
                avoidedIngredientIds = setOf("glycerin")
            )
        )
        assertFalse(assessment.suitableForUser)
        assertEquals(DangerLevel.SAFE, assessment.overall)
    }
}
