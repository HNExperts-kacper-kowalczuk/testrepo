package com.hnexperts.cosmetics.di

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

object AndroidAppContext {
    @Volatile
    private var application: Context? = null
    @Volatile
    private var activityRef: WeakReference<Activity>? = null

    fun install(context: Context) {
        application = context.applicationContext
    }

    fun attachActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun detachActivity(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    fun current(): Context? {
        return application
    }

    fun activity(): Activity? {
        return activityRef?.get()
    }
}
