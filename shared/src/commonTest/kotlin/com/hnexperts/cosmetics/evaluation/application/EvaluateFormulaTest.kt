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

    @Test
    fun euAllergenPresetMarksLimonene() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Limonene, Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(euAllergens = true)
        )
        assertFalse(assessment.suitableForUser)
        assertTrue(assessment.findings.first { finding -> finding.ingredient.id == "limonene" }.personalAvoid)
        assertEquals(DangerLevel.MODERATE, assessment.overall)
    }

    @Test
    fun childrenCautionMarksSalicylicAcid() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Salicylic Acid, Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(childrenCaution = true)
        )
        assertFalse(assessment.suitableForUser)
        assertTrue(assessment.findings.first { finding -> finding.ingredient.id == "salicylic-acid" }.personalAvoid)
    }

    @Test
    fun alcoholPresetSkipsRinseOffShampoo() {
        val rinseOff: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Alcohol Denat., Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(alcoholLeaveOn = true),
            usage = ProductUsage.RINSE_OFF
        )
        assertTrue(rinseOff.suitableForUser)
        assertFalse(rinseOff.findings.first { finding -> finding.ingredient.id == "alcohol-denat" }.personalAvoid)
    }

    @Test
    fun alcoholAssumedLeaveOnUntilRinseOffIsPicked() {
        val profile: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(alcoholLeaveOn = true)
        val inciRaw: String = "Aqua, Alcohol Denat., Glycerin"
        val assumed: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = inciRaw,
            profile = profile,
            usage = ProductUsage.UNKNOWN
        )
        val rinseOff: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = inciRaw,
            profile = profile,
            usage = ProductUsage.RINSE_OFF
        )
        assertTrue(assumed.usageAssumed)
        assertFalse(assumed.suitableForUser)
        assertFalse(rinseOff.usageAssumed)
        assertTrue(rinseOff.suitableForUser)
        assertEquals(DangerLevel.MODERATE, assumed.overall)
        assertEquals(DangerLevel.LOW, rinseOff.overall)
    }

    @Test
    fun alcoholPresetFlagsLeaveOnMist() {
        val leaveOn: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Alcohol Denat., Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(alcoholLeaveOn = true),
            usage = ProductUsage.LEAVE_ON
        )
        assertFalse(leaveOn.suitableForUser)
        assertTrue(leaveOn.findings.first { finding -> finding.ingredient.id == "alcohol-denat" }.personalAvoid)
        assertEquals(DangerLevel.MODERATE, leaveOn.overall)
    }

    @Test
    fun essentialOilPresetFlagsLimoneneNotHydroxycitronellal() {
        val withLimonene: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Limonene, Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(essentialOilCluster = true)
        )
        val withAllergenOnly: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Hydroxycitronellal, Glycerin",
            profile = UserAvoidanceProfile.EMPTY.copy(essentialOilCluster = true)
        )
        assertFalse(withLimonene.suitableForUser)
        assertTrue(withAllergenOnly.suitableForUser)
    }

    @Test
    fun highConcernInFirstFiveIsEarlyListSignal() {
        val early: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Methylisothiazolinone, Aqua, Glycerin",
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.RINSE_OFF
        )
        val late: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Glycerin, Phenoxyethanol, Sodium Benzoate, Cocamidopropyl Betaine, Methylisothiazolinone",
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.RINSE_OFF
        )
        assertTrue(early.findings.first { finding -> finding.ingredient.id == "methylisothiazolinone" }.earlyListConcern())
        assertFalse(late.findings.first { finding -> finding.ingredient.id == "methylisothiazolinone" }.earlyListConcern())
        assertEquals(DangerLevel.HIGH, late.overall)
    }

    @Test
    fun prohibitedLastIsStillOverallProhibited() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Glycerin, Formaldehyde",
            profile = UserAvoidanceProfile.EMPTY
        )
        assertEquals(DangerLevel.PROHIBITED, assessment.overall)
        assertFalse(assessment.findings.first { finding -> finding.ingredient.id == "formaldehyde" }.earlyListConcern())
    }

    @Test
    fun phototoxicTagIsACommentBadgeNotANewDangerLevel() {
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Salicylic Acid, Glycerin",
            profile = UserAvoidanceProfile.EMPTY
        )
        val salicylic = assessment.findings.first { finding -> finding.ingredient.id == "salicylic-acid" }
        assertTrue(salicylic.sunCaution())
        assertEquals(DangerLevel.RESTRICTED, salicylic.level)
        assertEquals(DangerLevel.RESTRICTED, assessment.overall)
        assertFalse(assessment.findings.first { finding -> finding.ingredient.id == "aqua" }.sunCaution())
    }

    @Test
    fun microplasticTagIsAChipNotASafetyScore() {
        val product = FixtureCatalog.products.first { item -> item.product.id == "bead-scrub" }.product
        val assessment: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = product.inciRaw,
            profile = UserAvoidanceProfile.EMPTY,
            usage = ProductUsage.parse(product.usage)
        )
        assertEquals(DangerLevel.SAFE, assessment.overall)
        assertTrue(assessment.hasMicroplastics())
        assertTrue(assessment.findings.first { finding -> finding.ingredient.id == "polyethylene" }.microplastic())
        assertFalse(assessment.findings.first { finding -> finding.ingredient.id == "aqua" }.microplastic())
        val withoutBeads: ProductAssessment = evaluateFormula.evaluate(
            inciRaw = "Aqua, Glycerin",
            profile = UserAvoidanceProfile.EMPTY
        )
        assertEquals(DangerLevel.SAFE, withoutBeads.overall)
        assertFalse(withoutBeads.hasMicroplastics())
    }
}
