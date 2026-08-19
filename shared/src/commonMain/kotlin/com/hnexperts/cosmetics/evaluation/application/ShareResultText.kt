package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment

object ShareResultText {
    const val DISCLAIMER: String =
        "Informational only. This is not a medical device or a substitute for the ingredient list, a dermatologist, or official EU annexes."

    fun format(assessment: ProductAssessment): String {
        val name: String = assessment.productName ?: assessment.gtin ?: "Scanned product"
        val brand: String = assessment.brand?.let { value -> " ($value)" }.orEmpty()
        val suitable: String = if (assessment.suitableForUser) {
            "No personal avoid-list hits"
        } else {
            "Not suitable for current filters"
        }
        return buildString {
            append(name)
            append(brand)
            append('\n')
            append(assessment.overall.name)
            append('\n')
            append(suitable)
            append('\n')
            append(DISCLAIMER)
        }
    }
}
