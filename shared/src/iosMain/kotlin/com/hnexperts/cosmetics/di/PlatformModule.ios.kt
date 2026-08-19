package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.ads.domain.AdsInitializer
import com.hnexperts.cosmetics.ads.domain.ConsentClient
import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.ads.ios.IosAdsInitializer
import com.hnexperts.cosmetics.ads.ios.IosConsentClient
import com.hnexperts.cosmetics.ads.ios.IosNetworkMonitor
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.IosDatabaseDriverFactory
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.ios.IosPerspectiveCropper
import com.hnexperts.cosmetics.scanning.ios.VisionIngredientListRecognizer
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    single<IngredientListRecognizer> { VisionIngredientListRecognizer(get()) }
    single<PerspectiveCropper> { IosPerspectiveCropper(get()) }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single<ConsentClient> { IosConsentClient() }
    single<AdsInitializer> { IosAdsInitializer() }
}
