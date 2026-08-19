package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CachedOnlineProduct
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.failure.Outcome
import kotlin.time.Clock

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
 * Offline catalog, then on-device online cache, then the network.
 * Successful network hits are stored so the next scan of that GTIN is offline.
 */
class ResolveGtin(
    private val offline: ResolveBarcode,
    private val cache: OnlineProductCache,
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
            is BarcodeLookup.NotFound -> resolveMiss(lookup.gtin)
        }
    }

    private suspend fun resolveMiss(gtin: String): Outcome<GtinResolution> {
        return when (val cached: Outcome<CachedOnlineProduct?> = cache.find(gtin)) {
            is Outcome.Err -> cached
            is Outcome.Ok -> {
                val hit: CachedOnlineProduct? = cached.value
                if (hit != null) {
                    Outcome.Ok(fromCache(hit))
                } else {
                    fallbackOnline(gtin)
                }
            }
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

    private fun fromCache(hit: CachedOnlineProduct): GtinResolution.ReadyToEvaluate {
        return GtinResolution.ReadyToEvaluate(
            gtin = hit.gtin,
            inciRaw = hit.inciRaw,
            productName = hit.name,
            brand = hit.brand,
            usage = ProductUsage.parse(hit.usage),
            source = hit.source
        )
    }

    private suspend fun fromOnline(gtin: String, hit: OnlineGtinHit): GtinResolution {
        return when (hit) {
            is OnlineGtinHit.WithIngredients -> {
                remember(hit)
                GtinResolution.ReadyToEvaluate(
                    gtin = hit.gtin,
                    inciRaw = hit.inciRaw,
                    productName = hit.name,
                    brand = hit.brand,
                    usage = hit.usage,
                    source = "online"
                )
            }
            is OnlineGtinHit.MissingIngredients -> GtinResolution.Unknown(gtin, onlineNoIngredients = true)
            is OnlineGtinHit.NotFound -> GtinResolution.Unknown(gtin, onlineNoIngredients = false)
        }
    }

    private suspend fun remember(hit: OnlineGtinHit.WithIngredients) {
        cache.put(
            CachedOnlineProduct(
                gtin = hit.gtin,
                name = hit.name,
                brand = hit.brand,
                inciRaw = hit.inciRaw,
                usage = hit.usage.name,
                source = "online",
                cachedAt = Clock.System.now().toString()
            )
        )
    }
}
