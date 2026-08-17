package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.IosDatabaseDriverFactory
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.ios.VisionIngredientListRecognizer
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    single<IngredientListRecognizer> { VisionIngredientListRecognizer(get()) }
}
