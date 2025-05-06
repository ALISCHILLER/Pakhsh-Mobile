package com.zar.core.di

import android.content.Context
import com.zar.core.BuildConfig
import com.zar.core.data.network.handler.HttpClientFactory
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
import okhttp3.Cache
import java.util.Locale
import java.util.concurrent.TimeUnit

val networkModule = module {

    // Network Config
    single { NetworkConfig.DEFAULT }

    // Network Monitor
    single<NetworkStatusMonitor> { NetworkStatusMonitor(androidContext()) }

    // HttpClient (با استفاده از HttpClientFactory)
    single {
        HttpClientFactory.create(androidContext(), get()).value
    }

    // NetworkHandler
    single {
        NetworkHandler.apply {
            initialize(
                context = androidContext(),
                monitor = get(),
                httpClient = get()
            )
        }
    }
}