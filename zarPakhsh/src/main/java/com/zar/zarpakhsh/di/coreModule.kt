package com.zar.zarpakhsh.di

import android.content.Context
import com.zar.core.config.AppConfig
import com.zar.core.data.network.handler.HttpClientFactory
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.utils.config.AppConfigZar
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {


    // Network Config
    single { NetworkConfig.DEFAULT }

    // Network Monitor
    single<NetworkStatusMonitor> { NetworkStatusMonitor(androidContext()) }

    // HttpClient
    single<HttpClient> {
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
    // AppConfig
    single<AppConfig> { AppConfigZar }
}