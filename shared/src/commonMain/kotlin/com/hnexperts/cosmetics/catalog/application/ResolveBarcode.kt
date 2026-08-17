package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.failure.Outcome

sealed class BarcodeLookup {
    data class Found(val product: Product, val gtin: String) : BarcodeLookup()
    data class NotFound(val gtin: String) : BarcodeLookup()
    data object Invalid : BarcodeLookup()
}

class ResolveBarcode(
    private val products: ProductRepository
) {
    suspend fun invoke(raw: String): Outcome<BarcodeLookup> {
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            return Outcome.Ok(BarcodeLookup.Invalid)
        }
        return when (val found: Outcome<Product?> = products.findByGtin(gtin)) {
            is Outcome.Err -> found
            is Outcome.Ok -> {
                val product: Product? = found.value
                if (product == null) {
                    Outcome.Ok(BarcodeLookup.NotFound(gtin))
                } else {
                    Outcome.Ok(BarcodeLookup.Found(product, gtin))
                }
            }
        }
    }
}
