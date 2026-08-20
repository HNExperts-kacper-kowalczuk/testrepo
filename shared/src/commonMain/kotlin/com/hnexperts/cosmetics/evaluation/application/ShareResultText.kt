package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import kotlin.time.Clock
import kotlin.time.Instant

data class ShareCopy(
    val scannedProduct: String,
    val suitable: String,
    val notSuitable: String,
    val disclaimer: String,
    val overallLabel: String,
    val scannedAtLabel: String
)

object ShareResultText {
    fun format(
        assessment: ProductAssessment,
        copy: ShareCopy,
        scannedAt: Instant = Clock.System.now()
    ): String {
        val name: String = assessment.productName ?: assessment.gtin ?: copy.scannedProduct
        val brand: String = assessment.brand?.let { value -> " ($value)" }.orEmpty()
        val suitable: String = if (assessment.suitableForUser) copy.suitable else copy.notSuitable
        return buildString {
            append(name)
            append(brand)
            append('\n')
            append(copy.overallLabel)
            append('\n')
            append(suitable)
            append('\n')
            append(copy.scannedAtLabel)
            append(' ')
            append(scannedAt.toString().substringBefore('T'))
            append('\n')
            append(copy.disclaimer)
        }
    }
}
