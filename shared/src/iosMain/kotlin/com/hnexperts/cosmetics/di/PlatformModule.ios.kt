package com.hnexperts.cosmetics.di

import com.hnexperts.cosmetics.data.DatabaseDriverFactory
import com.hnexperts.cosmetics.data.IosDatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
}
