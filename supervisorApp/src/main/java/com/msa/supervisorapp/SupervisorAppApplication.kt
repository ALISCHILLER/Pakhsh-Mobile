package com.msa.supervisorapp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.persistence.di.persistenceModule
import com.msa.supervisorApp.di.supervisorAppConfigModule
import com.msa.supervisorApp.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SupervisorAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@SupervisorAppApplication)
            modules(
                supervisorAppConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
            )
        }
    }
}