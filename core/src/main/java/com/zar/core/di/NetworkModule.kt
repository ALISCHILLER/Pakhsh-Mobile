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
    // NetworkConfig Single
    single { NetworkConfig.DEFAULT }

    // NetworkStatusMonitor برای نظارت بر وضعیت اتصال شبکه
    single { NetworkStatusMonitor(androidContext()) }

    // HttpClient تنظیمات اصلی HttpClient برای Ktor
    single(named("ktor_client")) {
        HttpClientFactory.create(androidContext(), get())
    }

    // پیاده‌سازی NetworkHandler و مقداردهی اولیه
    single {
        NetworkHandler.initialize(
            context = androidContext(),
            monitor = get(),
            httpClient = get(named("ktor_client"))
        )
        NetworkHandler
    }
}
