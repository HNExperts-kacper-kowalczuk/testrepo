package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.ads.jvm.JvmAdsInitializer
import com.hnexperts.cosmetics.ads.jvm.JvmConsentClient
import com.hnexperts.cosmetics.ads.jvm.JvmNetworkMonitor
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.JvmDatabaseDriverFactory
import com.hnexperts.cosmetics.network.SimpleHttpClient
import com.hnexperts.cosmetics.network.UrlConnectionHttpClient
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.jvm.UnsupportedIngredientListRecognizer
import com.hnexperts.cosmetics.scanning.jvm.UnsupportedPerspectiveCropper
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { JvmDatabaseDriverFactory() }
    single<IngredientListRecognizer> { UnsupportedIngredientListRecognizer() }
    single<PerspectiveCropper> { UnsupportedPerspectiveCropper() }
    single<SimpleHttpClient> { UrlConnectionHttpClient(get()) }
    single<NetworkMonitor> { JvmNetworkMonitor() }
    single<ConsentClient> { JvmConsentClient() }
    single<AdsInitializer> { JvmAdsInitializer() }
}
