package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel

data class ComparedProduct(
    val id: String,
    val label: String,
    val assessment: ProductAssessment
)

data class CompareSummary(
    val products: List<ComparedProduct>,
    val uniqueHighOrProhibited: Map<String, List<String>>,
    val sharedPersonalAvoids: List<String>
)

object CompareFormulas {
    fun summarize(products: List<ComparedProduct>): CompareSummary {
        val concernNames: Map<String, Set<String>> = products.associate { product ->
            product.id to highOrProhibitedNames(product.assessment.findings)
        }
        val unique: Map<String, List<String>> = concernNames.mapValues { entry ->
            val others: Set<String> = concernNames
                .filter { other -> other.key != entry.key }
                .flatMap { other -> other.value }
                .toSet()
            (entry.value - others).sorted()
        }
        return CompareSummary(
            products = products,
            uniqueHighOrProhibited = unique,
            sharedPersonalAvoids = sharedAvoids(products)
        )
    }

    fun fromAssessments(
        assessments: List<ProductAssessment>,
        unnamedFormat: String = CompareSession.DEFAULT_UNNAMED_FORMAT
    ): CompareSummary {
        val products: List<ComparedProduct> = assessments.mapIndexed { index, assessment ->
            ComparedProduct(
                id = index.toString(),
                label = displayLabel(assessment, index, unnamedFormat),
                assessment = assessment
            )
        }
        return summarize(products)
    }

    fun displayLabel(assessment: ProductAssessment, index: Int, unnamedFormat: String): String {
        return assessment.productName
            ?: assessment.gtin
            ?: unnamedFormat.replace("{n}", (index + 1).toString())
    }

    private fun sharedAvoids(products: List<ComparedProduct>): List<String> {
        val avoidSets: List<Set<String>> = products.map { product ->
            product.assessment.findings
                .filter { finding -> finding.personalAvoid }
                .map { finding -> finding.ingredient.displayName }
                .toSet()
        }
        if (avoidSets.isEmpty()) {
            return emptyList()
        }
        return avoidSets.reduce { left, right -> left.intersect(right) }.sorted()
    }

    private fun highOrProhibitedNames(findings: List<Finding>): Set<String> {
        return findings
            .filter { finding -> finding.level == DangerLevel.HIGH || finding.level == DangerLevel.PROHIBITED }
            .map { finding -> finding.ingredient.displayName }
            .toSet()
    }
}
