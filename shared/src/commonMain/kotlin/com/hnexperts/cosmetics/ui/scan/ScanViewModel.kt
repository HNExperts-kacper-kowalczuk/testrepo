package com.hnexperts.cosmetics.ui.scan

import androidx.lifecycle.ViewModel
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository

sealed class BarcodeLookup {
    data class Found(val assessment: ProductAssessment) : BarcodeLookup()
    data class NotFound(val gtin: String) : BarcodeLookup()
    data class Invalid(val reason: String) : BarcodeLookup()
}

class ScanViewModel(
    private val products: SqlProductRepository,
    private val index: CatalogIndex,
    private val session: EvaluationSession,
    private val preferences: SqlPreferencesRepository,
    private val history: SqlHistoryRepository
) : ViewModel() {
    fun lookupBarcode(raw: String): BarcodeLookup {
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            return BarcodeLookup.Invalid("short")
        }
        val product: Product? = products.findByGtin(gtin)
        if (product == null) {
            return BarcodeLookup.NotFound(gtin)
        }
        val assessment: ProductAssessment = evaluateAndStore(
            inciRaw = product.inciRaw,
            source = "barcode",
            productName = product.name,
            brand = product.brand,
            gtin = gtin
        )
        return BarcodeLookup.Found(assessment)
    }

    fun evaluateTypedList(inciRaw: String): ProductAssessment {
        return evaluateAndStore(
            inciRaw = inciRaw,
            source = "manual",
            productName = null,
            brand = null,
            gtin = null
        )
    }

    private fun evaluateAndStore(
        inciRaw: String,
        source: String,
        productName: String?,
        brand: String?,
        gtin: String?
    ): ProductAssessment {
        val profile = preferences.load().profile
        val assessment: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = inciRaw,
            profile = profile,
            productName = productName,
            brand = brand,
            gtin = gtin
        )
        session.lastAssessment = assessment
        session.lastSource = source
        history.record(assessment, source)
        return assessment
    }
}
