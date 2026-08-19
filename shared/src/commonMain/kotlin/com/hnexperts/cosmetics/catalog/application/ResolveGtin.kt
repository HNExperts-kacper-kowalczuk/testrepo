package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.failure.Outcome

sealed class GtinResolution {
    data class ReadyToEvaluate(
        val gtin: String,
        val inciRaw: String,
        val productName: String?,
        val brand: String?,
        val usage: ProductUsage,
        val source: String
    ) : GtinResolution()

    data class Unknown(
        val gtin: String,
        val onlineNoIngredients: Boolean
    ) : GtinResolution()

    data object Invalid : GtinResolution()
}

/**
 * Offline catalog first. If there is no local hit and the device is online,
 * fetch the printed INCI list from Open Beauty Facts and return it ready to score.
 */
class ResolveGtin(
    private val offline: ResolveBarcode,
    private val online: OnlineGtinLookup
) {
    suspend fun invoke(raw: String): Outcome<GtinResolution> {
        return when (val lookup: Outcome<BarcodeLookup> = offline.invoke(raw)) {
            is Outcome.Err -> lookup
            is Outcome.Ok -> resolve(lookup.value)
        }
    }

    private suspend fun resolve(lookup: BarcodeLookup): Outcome<GtinResolution> {
        return when (lookup) {
            BarcodeLookup.Invalid -> Outcome.Ok(GtinResolution.Invalid)
            is BarcodeLookup.Found -> Outcome.Ok(fromCatalog(lookup))
            is BarcodeLookup.NotFound -> fallbackOnline(lookup.gtin)
        }
    }

    private suspend fun fallbackOnline(gtin: String): Outcome<GtinResolution> {
        return when (val hit: Outcome<OnlineGtinHit> = online.invoke(gtin)) {
            is Outcome.Err -> Outcome.Ok(GtinResolution.Unknown(gtin, onlineNoIngredients = false))
            is Outcome.Ok -> Outcome.Ok(fromOnline(gtin, hit.value))
        }
    }

    private fun fromCatalog(lookup: BarcodeLookup.Found): GtinResolution.ReadyToEvaluate {
        return GtinResolution.ReadyToEvaluate(
            gtin = lookup.gtin,
            inciRaw = lookup.product.inciRaw,
            productName = lookup.product.name,
            brand = lookup.product.brand,
            usage = ProductUsage.parse(lookup.product.usage),
            source = "barcode"
        )
    }

    private fun fromOnline(gtin: String, hit: OnlineGtinHit): GtinResolution {
        return when (hit) {
            is OnlineGtinHit.WithIngredients -> GtinResolution.ReadyToEvaluate(
                gtin = hit.gtin,
                inciRaw = hit.inciRaw,
                productName = hit.name,
                brand = hit.brand,
                usage = hit.usage,
                source = "online"
            )
            is OnlineGtinHit.MissingIngredients -> GtinResolution.Unknown(gtin, onlineNoIngredients = true)
            is OnlineGtinHit.NotFound -> GtinResolution.Unknown(gtin, onlineNoIngredients = false)
        }
    }
}
