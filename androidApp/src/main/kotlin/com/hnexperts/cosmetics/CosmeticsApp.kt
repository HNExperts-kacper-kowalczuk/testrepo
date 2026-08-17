package com.hnexperts.cosmetics

import android.app.Application
import com.hnexperts.cosmetics.di.AndroidAppContext
import com.hnexperts.cosmetics.di.initKoin
import org.koin.android.ext.koin.androidContext

class CosmeticsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CosmeticsApp)
        }
        AndroidAppContext.install(this)
    }
}
