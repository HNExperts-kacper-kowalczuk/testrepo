package com.hnexperts.cosmetics.di

import android.content.Context

object AndroidAppContext {
    @Volatile
    private var application: Context? = null

    fun install(context: Context) {
        application = context.applicationContext
    }

    fun current(): Context? {
        return application
    }
}
