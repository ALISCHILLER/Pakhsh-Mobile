package com.msa.visitApp

import android.app.Application
import com.msa.visitApp.di.visitDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class visitApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())  // فقط در حالت DEBUG از DebugTree استفاده کن
        }
    }
}