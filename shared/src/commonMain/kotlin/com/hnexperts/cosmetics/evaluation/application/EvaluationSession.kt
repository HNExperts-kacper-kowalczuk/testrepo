package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment

class EvaluationSession {
    var lastAssessment: ProductAssessment? = null
    var lastSource: String = "manual"
}
