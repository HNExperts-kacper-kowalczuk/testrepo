package com.hnexperts.cosmetics.evaluation.domain

import com.hnexperts.cosmetics.hazards.domain.DangerLevel

data class ResultA11yCounts(
    val prohibited: Int,
    val high: Int,
    val unknown: Int
) {
    companion object {
        fun of(assessment: ProductAssessment): ResultA11yCounts {
            return ResultA11yCounts(
                prohibited = assessment.findings.count { finding -> finding.level == DangerLevel.PROHIBITED },
                high = assessment.findings.count { finding -> finding.level == DangerLevel.HIGH },
                unknown = assessment.unknownCount
            )
        }
    }
}
