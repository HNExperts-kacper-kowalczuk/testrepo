package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile

data class CatalogAlternative(
    val product: Product,
    val assessment: ProductAssessment
)

object FindLocalAlternatives {
    const val CANDIDATE_CAP: Int = 20
    const val RESULT_CAP: Int = 3
    const val CATEGORY_PICK_CAP: Int = 20

    fun invoke(
        current: ProductAssessment,
        candidates: List<Product>,
        evaluateFormula: EvaluateFormula,
        profile: UserAvoidanceProfile
    ): List<CatalogAlternative> {
        val category: String = current.category?.trim().orEmpty()
        if (category.isEmpty()) {
            return emptyList()
        }
        return candidates
            .asSequence()
            .filter { product -> product.category == category }
            .filter { product -> product.inciRaw != current.inciRaw }
            .take(CANDIDATE_CAP)
            .map { product ->
                CatalogAlternative(
                    product = product,
                    assessment = evaluateFormula.evaluate(
                        inciRaw = product.inciRaw,
                        profile = profile,
                        productName = product.name,
                        brand = product.brand,
                        usage = ProductUsage.parse(product.usage)
                    )
                )
            }
            .filter { alternative -> betterOverall(alternative.assessment.overall, current.overall) }
            .sortedBy { alternative -> rank(alternative.assessment.overall) }
            .take(RESULT_CAP)
            .toList()
    }

    private fun betterOverall(candidate: DangerLevel, current: DangerLevel): Boolean {
        if (!candidate.isMatchedHazard()) {
            return false
        }
        if (!current.isMatchedHazard()) {
            return true
        }
        return rank(candidate) < rank(current)
    }

    private fun rank(level: DangerLevel): Int {
        return when (level) {
            DangerLevel.SAFE -> 1
            DangerLevel.LOW -> 2
            DangerLevel.MODERATE -> 3
            DangerLevel.RESTRICTED -> 4
            DangerLevel.HIGH -> 5
            DangerLevel.PROHIBITED -> 6
            DangerLevel.UNKNOWN -> 7
        }
    }
}
