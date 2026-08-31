package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.InciRawEditor

class ReplaceUnmatchedIngredient(
    private val catalog: CatalogGateway
) {
    suspend fun invoke(inciRaw: String, listIndex: Int, catalogName: String): Outcome<String> {
        val indexOutcome: Outcome<CatalogIndex> = catalog.awaitIndex()
        val index: CatalogIndex = when (indexOutcome) {
            is Outcome.Err -> return indexOutcome
            is Outcome.Ok -> indexOutcome.value
        }
        return FailureCatcher.evaluation("evaluation.replaceUnmatched") {
            InciRawEditor.replaceAt(index.matcher.tokenize(inciRaw), listIndex, catalogName)
        }
    }
}
