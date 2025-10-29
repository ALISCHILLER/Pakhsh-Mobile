package com.msa.supervisorapp

import android.app.Application
import com.msa.core.di.coreModule
import com.msa.core.di.utilsModule
import com.msa.persistence.di.persistenceModule
import com.msa.supervisorapp.di.supervisorappConfigModule
import com.msa.supervisorapp.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class supervisorappApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@supervisorappApplication)
            modules(
                supervisorappConfigModule,
                coreModule,
                utilsModule,
                persistenceModule,
            )
        }
    }
}