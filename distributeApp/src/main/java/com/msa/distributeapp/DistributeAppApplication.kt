package com.msa.distributeApp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.distributeApp.BuildConfig
import com.msa.distributeApp.di.distributeAppConfigModule
import com.msa.persistence.di.persistenceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class DistributeAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@DistributeAppApplication)
            modules(
                distributeAppConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
            )
        }
    }
}