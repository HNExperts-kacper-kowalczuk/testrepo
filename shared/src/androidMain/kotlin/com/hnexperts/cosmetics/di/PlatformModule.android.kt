package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.ads.android.AndroidAdsInitializer
import com.hnexperts.cosmetics.ads.android.AndroidConsentClient
import com.hnexperts.cosmetics.ads.android.AndroidNetworkMonitor
import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.data.AndroidDatabaseDriverFactory
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.network.SimpleHttpClient
import com.hnexperts.cosmetics.network.UrlConnectionHttpClient
import com.hnexperts.cosmetics.scanning.android.AndroidPerspectiveCropper
import com.hnexperts.cosmetics.scanning.android.MlKitIngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single<IngredientListRecognizer> { MlKitIngredientListRecognizer(get()) }
    single<PerspectiveCropper> { AndroidPerspectiveCropper(get()) }
    single<SimpleHttpClient> { UrlConnectionHttpClient(get()) }
    single<NetworkMonitor> { AndroidNetworkMonitor() }
    single<ConsentClient> { AndroidConsentClient() }
    single<AdsInitializer> { AndroidAdsInitializer() }
}
