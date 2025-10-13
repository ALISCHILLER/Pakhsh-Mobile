package com.zar.core.di

import com.zar.core.data.network.error.NetworkErrorMapper
import com.zar.core.data.network.handler.HttpClientFactory
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {


    single { NetworkConfig.DEFAULT }

    single { HttpClientFactory.create(androidContext(), get()) }


    single { NetworkStatusMonitor(androidContext(), get()) }

    single { NetworkErrorMapper(androidContext()) }


    single {
        NetworkHandler(
            client = get(),
            config = get(),
            networkMonitor = get(),
            errorMapper = get(),
        )
    }
}