package com.msa.distributeapp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.distributeapp.BuildConfig
import com.msa.distributeapp.di.distributeappConfigModule
import com.msa.persistence.di.persistenceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class distributeappApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@distributeappApplication)
            modules(
                distributeappConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
            )
        }
    }
}