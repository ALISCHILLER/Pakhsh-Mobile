package com.zar.zarvisitapp

import android.app.Application
import com.zar.zarvisitapp.di.visitDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class ZarVisitApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())  // فقط در حالت DEBUG از DebugTree استفاده کن
        }
    }
}