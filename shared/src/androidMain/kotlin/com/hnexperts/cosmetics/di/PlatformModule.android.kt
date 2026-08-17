package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.data.AndroidDatabaseDriverFactory
import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.scanning.android.MlKitIngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single<IngredientListRecognizer> { MlKitIngredientListRecognizer(get()) }
}
