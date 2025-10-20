package com.msa.distributeApp

import android.app.Application
import com.msa.msapakhsh.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class distributeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@distributeApp)
            modules(appModule)
        }
    }
}