package com.zar.core.di

import com.zar.core.data.network.client.HttpClientFactory
import com.zar.core.data.network.client.NetworkClient
import com.zar.core.data.network.common.AndroidStringProvider
import com.zar.core.data.network.common.StringProvider
import com.zar.core.data.network.error.ErrorMapper
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {


    single { NetworkConfig.DEFAULT }

    single<StringProvider> { AndroidStringProvider(androidContext()) }
    single { ErrorMapper(get()) }


    single { NetworkStatusMonitor(androidContext()) }

    single { HttpClientFactory.create(androidContext(), get()) }


    single {
        NetworkClient(
            httpClient = get(),
            statusMonitor = get(),
            stringProvider = get(),
            errorMapper = get()
        )
    }
}