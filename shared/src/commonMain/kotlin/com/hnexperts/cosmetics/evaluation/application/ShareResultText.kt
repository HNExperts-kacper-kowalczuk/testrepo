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

data class ShareResultImageLayout(
    val title: String,
    val rating: String,
    val suitableLine: String,
    val dateLine: String,
    val disclaimer: String
) {
    fun payloadText(): String {
        return listOf(title, rating, suitableLine, dateLine, disclaimer).joinToString(separator = "\n")
    }

    fun drawLines(): List<String> {
        return listOf(title, rating, suitableLine, dateLine) + wrap(disclaimer, DISCLAIMER_WRAP)
    }

    private fun wrap(text: String, maxChars: Int): List<String> {
        if (text.length <= maxChars) {
            return listOf(text)
        }
        val words: List<String> = text.split(' ')
        val lines: MutableList<String> = mutableListOf()
        val current = StringBuilder()
        for (word in words) {
            if (current.isNotEmpty() && current.length + word.length + 1 > maxChars) {
                lines.add(current.toString())
                current.clear()
            }
            if (current.isNotEmpty()) {
                current.append(' ')
            }
            current.append(word)
        }
        if (current.isNotEmpty()) {
            lines.add(current.toString())
        }
        return lines
    }

    private companion object {
        const val DISCLAIMER_WRAP: Int = 42
    }
}

object ShareResultText {
    fun format(
        assessment: ProductAssessment,
        copy: ShareCopy,
        scannedAt: Instant = Clock.System.now()
    ): String {
        return layout(assessment, copy, scannedAt).payloadText()
    }

    fun layout(
        assessment: ProductAssessment,
        copy: ShareCopy,
        scannedAt: Instant = Clock.System.now()
    ): ShareResultImageLayout {
        val name: String = assessment.productName ?: assessment.gtin ?: copy.scannedProduct
        val brand: String = assessment.brand?.let { value -> " ($value)" }.orEmpty()
        val suitable: String = if (assessment.suitableForUser) copy.suitable else copy.notSuitable
        val date: String = scannedAt.toString().substringBefore('T')
        return ShareResultImageLayout(
            title = name + brand,
            rating = copy.overallLabel,
            suitableLine = suitable,
            dateLine = "${copy.scannedAtLabel} $date",
            disclaimer = copy.disclaimer
        )
    }
}
