package com.msa.core.di


import com.google.gson.Gson
import com.msa.core.common.config.AppConfig
import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.common.text.StringProvider
import com.msa.core.common.time.Clock
import com.msa.core.logging.api.Logger
import com.msa.core.logging.impl.FileLogger

import com.msa.core.network.auth.AuthOrchestrator
import com.msa.core.network.auth.TokenStore
import com.msa.core.network.cache.HttpCacheRepository
import com.msa.core.network.cache.HttpCacheRepositoryImpl
import com.msa.core.network.client.EnvelopeApi
import com.msa.core.network.client.HttpClientFactory
import com.msa.core.network.client.NetworkClient
import com.msa.core.network.client.RawApi
import com.msa.core.network.config.NetworkConfig
import com.msa.core.network.config.SSLConfig
import com.msa.core.network.circuit.CircuitBreaker
import com.msa.core.network.error.ErrorMapper
import com.msa.core.network.error.ErrorMapperImpl
import com.msa.core.network.client.NetworkStatusMonitor
import com.msa.core.storage.prefs.BaseSharedPreferences
import com.msa.core.validation.EnhancedNumberConverter
import com.msa.core.validation.NationalCodeValidator
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.getOrNull
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }
    single { Clock.System }

    single<StringProvider> { AndroidStringProvider(androidContext()) }
    single<ErrorMapper> { ErrorMapperImpl(get()) }

    single { buildNetworkConfig(get()) }
    single<HttpCacheRepository> { HttpCacheRepositoryImpl() }
    single { CircuitBreaker(get<NetworkConfig>().circuit) }

    single { AndroidNetworkStatusMonitor(androidContext().applicationContext) } bind NetworkStatusMonitor::class

    single {
        HttpClientFactory.create(
            config = get(),
            tokenStore = getOrNull(),
            authOrchestrator = getOrNull(),
            cacheDir = androidContext().cacheDir.resolve("http-cache")
        )
    }


    single {
        NetworkClient(
            httpClient = get(),
            statusMonitor = get(),
            errorMapper = get(),
            cacheRepository = get(),
            circuitBreaker = get(),
            config = get()
        )
    }

    single<RawApi> { get<NetworkClient>() }
    single<EnvelopeApi> { get<NetworkClient>() }
}

val utilsModule = module {
    single { Gson() }

    single {
        BaseSharedPreferences(
            context = androidContext(),
            prefsName = get<AppConfig>().sharedPreferencesName,
            isEncrypted = true,
            gson = get(),
        )
    }

    single { EnhancedNumberConverter }
    single { NationalCodeValidator }

    single<Logger> { FileLogger(androidContext().filesDir.resolve("logs")) }
}

private fun buildNetworkConfig(appConfig: AppConfig): NetworkConfig =
    NetworkConfig(
        baseUrl = appConfig.baseUrl,
        loggingEnabled = appConfig.enableLogging,
        defaultHeaders = emptyMap(),
        ssl = SSLConfig(pinningEnabled = appConfig.sslPinningEnabled)
    )