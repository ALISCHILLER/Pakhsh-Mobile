package com.zar.core.di

import com.zar.core.data.network.model.NetworkConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {


    single { NetworkConfig.DEFAULT }

    single { NetworkErrorMapper() }
    single { NetworkStatusMonitor(androidContext()) }

    single { HttpClientFactory.create(androidContext(), get()) }


    single {
        NetworkHandler(
            client = get(),
            config = get(),
            networkMonitor = get(),
            errorMapper = get(),
        )
    }
}