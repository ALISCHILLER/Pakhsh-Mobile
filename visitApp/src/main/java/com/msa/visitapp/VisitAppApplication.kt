package com.msa.visitapp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.persistence.di.persistenceModule
import com.msa.visitapp.di.visitDatabaseModule
import com.msa.visitapp.BuildConfig
import com.msa.visitapp.di.visitappConfigModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class visitappApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@visitappApplication)
            modules(
                visitappConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
                visitDatabaseModule,
            )
        }
    }
}