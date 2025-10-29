package com.msa.visitApp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.persistence.di.persistenceModule
import com.msa.visitApp.di.visitAppConfigModule
import com.msa.visitApp.di.visitDatabaseModule
import com.msa.visitApp.BuildConfig
import timber.log.Timber

class VisitAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@VisitAppApplication)
            modules(
                visitAppConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
                visitDatabaseModule,
            )
        }
    }
}