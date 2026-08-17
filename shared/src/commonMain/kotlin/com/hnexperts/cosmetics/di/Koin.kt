package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.catalog.application.CatalogBootstrap
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository
import com.hnexperts.cosmetics.ui.history.HistoryViewModel
import com.hnexperts.cosmetics.ui.preferences.PreferencesViewModel
import com.hnexperts.cosmetics.ui.result.ResultViewModel
import com.hnexperts.cosmetics.ui.scan.ScanViewModel
import com.hnexperts.cosmetics.ui.search.SearchViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { AppDispatchers() }
    single { CatalogDatabase(get<DatabaseDriverFactory>().createCatalogDriver()) }
    single { UserDatabase(get<DatabaseDriverFactory>().createUserDriver()) }
    single { CatalogBootstrap(get(), get()) }
    single { SqlProductRepository(get(), get()) }
    single { SqlPreferencesRepository(get(), get()) }
    single { SqlHistoryRepository(get(), get()) }
    single { EvaluationSession() }
    single { CommentLocalizer() }
    single { EvaluateProduct(get(), get(), get(), get(), get()) }

    viewModelOf(::ScanViewModel)
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
