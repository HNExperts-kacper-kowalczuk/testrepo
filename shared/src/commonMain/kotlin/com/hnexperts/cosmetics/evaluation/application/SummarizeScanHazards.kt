package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry

class SummarizeScanHazards {
    fun invoke(
        entries: List<HistoryEntry>,
        evaluateFormula: EvaluateFormula,
        profile: UserAvoidanceProfile
    ): List<String> {
        if (entries.size < MIN_SCANS) {
            return emptyList()
        }
        val counts: MutableMap<String, Int> = mutableMapOf()
        for (entry in entries.take(HISTORY_CAP)) {
            val assessment: ProductAssessment = evaluateFormula.evaluate(
                inciRaw = entry.inciRaw,
                profile = profile,
                usage = entry.usage
            )
            addConcerns(counts, assessment.findings)
        }
        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { entry -> entry.value }.thenBy { entry -> entry.key })
            .take(TOP_LIMIT)
            .map { entry -> entry.key }
    }

    private fun addConcerns(counts: MutableMap<String, Int>, findings: List<Finding>) {
        for (finding in findings) {
            if (!isListedConcern(finding.level)) {
                continue
            }
            val name: String = finding.ingredient.displayName
            counts[name] = counts.getOrElse(name) { 0 } + 1
        }
    }

    private fun isListedConcern(level: DangerLevel): Boolean {
        return level == DangerLevel.HIGH || level == DangerLevel.PROHIBITED
    }

    private companion object {
        const val MIN_SCANS: Int = 2
        const val HISTORY_CAP: Int = 100
        const val TOP_LIMIT: Int = 5
    }
}
