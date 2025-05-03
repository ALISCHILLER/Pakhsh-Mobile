package com.zar.zardistributeapp

import android.app.Application
import com.zar.zarpakhsh.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZarDistributeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZarDistributeApp)
            modules(appModule)
        }
    }
}