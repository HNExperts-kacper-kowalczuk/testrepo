package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase
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
    single {
        val database: CatalogDatabase = CatalogDatabase(get<DatabaseDriverFactory>().createCatalogDriver())
        CatalogSeeder(database).seedIfEmpty()
        database
    }
    single { UserDatabase(get<DatabaseDriverFactory>().createUserDriver()) }
    single { SqlProductRepository(get()) }
    single { SqlPreferencesRepository(get()) }
    single { SqlHistoryRepository(get()) }
    single { CatalogIndex.load(get()) }
    single { EvaluationSession() }
    single { CommentLocalizer() }

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
