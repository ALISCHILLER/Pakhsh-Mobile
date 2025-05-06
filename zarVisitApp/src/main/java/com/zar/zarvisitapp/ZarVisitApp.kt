package com.zar.zarvisitapp

import android.app.Application
import com.zar.zarpakhsh.di.appModule
import com.zar.zarpakhsh.di.repositoryModule
import com.zar.zarpakhsh.di.useCaseModule
import com.zar.zarpakhsh.di.viewModelModule
import com.zar.zarvisitapp.di.visitDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZarVisitApp : Application() {
    override fun onCreate() {
        super.onCreate()

//        if (BuildConfig.DEBUG) {
//            Timber.plant(DebugTree())
//        }
        startKoin {
            androidContext(this@ZarVisitApp)
            modules(listOf(appModule, repositoryModule, useCaseModule, viewModelModule))
        }
    }
}