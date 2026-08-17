package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.ads.application.AdsSession
import com.hnexperts.cosmetics.catalog.application.BundledCatalogRemote
import com.hnexperts.cosmetics.catalog.application.CatalogBootstrap
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogRemote
import com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdates
import com.hnexperts.cosmetics.catalog.application.ResolveBarcode
import com.hnexperts.cosmetics.catalog.data.CatalogSnapshotReader
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.legal.data.SqlLegalRepository
import com.hnexperts.cosmetics.legal.domain.LegalStore
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.scanning.application.IngredientReviewSession
import com.hnexperts.cosmetics.scanning.application.PrepareIngredientReview
import com.hnexperts.cosmetics.scanning.application.ScanBridge
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import com.hnexperts.cosmetics.ui.camera.CameraScanViewModel
import com.hnexperts.cosmetics.ui.confirm.ConfirmIngredientsViewModel
import com.hnexperts.cosmetics.ui.legal.DisclaimerViewModel
import com.hnexperts.cosmetics.ui.history.HistoryViewModel
import com.hnexperts.cosmetics.ui.preferences.PreferencesViewModel
import com.hnexperts.cosmetics.ui.result.ResultViewModel
import com.hnexperts.cosmetics.ui.scan.ScanViewModel
import com.hnexperts.cosmetics.ui.search.SearchViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { AppDispatchers() }
    single { ApplicationScope(get()) }
    single { CatalogDatabase(get<DatabaseDriverFactory>().createCatalogDriver()) }
    single { UserDatabase(get<DatabaseDriverFactory>().createUserDriver()) }
    single { CatalogSeeder(get()) }
    single { CatalogSnapshotReader(get()) }
    single<CatalogGateway> { CatalogBootstrap(get(), get(), get(), get()) }
    single<ProductRepository> { SqlProductRepository(get(), get()) }
    single<PreferencesStore> { SqlPreferencesRepository(get(), get()) }
    single<ScanHistoryRepository> { SqlHistoryRepository(get(), get()) }
    single<LegalStore> { SqlLegalRepository(get(), get()) }
    single { EvaluationSession() }
    single { CommentLocalizer() }
    single { EvaluateProduct(get(), get(), get(), get(), get()) }
    single { ResolveBarcode(get()) }
    single { PrepareIngredientReview(get()) }
    single { IngredientReviewSession() }
    single { ScanBridge() }
    single<CatalogRemote> { BundledCatalogRemote() }
    single { CheckCatalogUpdates(get(), get(), get()) }
    single { AdsSession(get(), get(), get(), get()) }

    viewModelOf(::ScanViewModel)
    viewModel { parameters ->
        CameraScanViewModel(
            resolveBarcode = get(),
            evaluateProduct = get(),
            recognizer = get(),
            prepareReview = get(),
            reviewSession = get(),
            scanBridge = get(),
            initialMode = parameters.getOrNull<ScannerMode>() ?: ScannerMode.BARCODE
        )
    }
    viewModelOf(::DisclaimerViewModel)
    viewModelOf(::ConfirmIngredientsViewModel)
    viewModelOf(::ResultViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::PreferencesViewModel)
}

fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(platformModule(), appModule)
    }
}
