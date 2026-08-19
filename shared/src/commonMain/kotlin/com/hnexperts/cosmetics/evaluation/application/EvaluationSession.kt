package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EvaluationSession {
    private val mutex: Mutex = Mutex()
    private var lastAssessment: ProductAssessment? = null
    private var lastSource: String = "manual"

    suspend fun publish(assessment: ProductAssessment, source: String) {
        mutex.withLock {
            lastAssessment = assessment
            lastSource = source
        }
    }

    suspend fun currentSource(): String {
        return mutex.withLock { lastSource }
    }

    suspend fun currentAssessment(): ProductAssessment? {
        return mutex.withLock { lastAssessment }
    }
}
