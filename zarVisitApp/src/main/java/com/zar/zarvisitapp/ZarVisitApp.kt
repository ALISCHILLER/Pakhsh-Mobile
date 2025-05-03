package com.zar.zarvisitapp

import android.app.Application
import com.zar.zarpakhsh.di.appModule
import com.zar.zarpakhsh.di.dataLocalModule
import com.zar.zarvisitapp.di.visitDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZarVisitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZarVisitApp)
            modules(
                appModule,
                visitDatabaseModule,
            )
        }
    }
}