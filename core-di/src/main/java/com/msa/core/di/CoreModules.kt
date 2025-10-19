package com.msa.core.di

import com.zar.core.common.config.AppConfig
import com.zar.core.common.text.StringProvider
import com.zar.core.network.auth.AuthOrchestrator
import com.zar.core.network.auth.TokenStore
import com.zar.core.network.cache.HttpCacheRepository
import com.zar.core.network.cache.HttpCacheRepositoryImpl
import com.zar.core.network.circuit.CircuitBreaker
import com.zar.core.network.client.EnvelopeApi
import com.zar.core.network.client.HttpClientFactory
import com.zar.core.network.client.NetworkClient
import com.zar.core.network.client.RawApi
import com.zar.core.network.config.NetworkConfig
import com.zar.core.network.error.ErrorMapper
import com.zar.core.network.error.ErrorMapperImpl
import com.zar.core.network.status.NetworkStatusMonitor
import com.zar.core.storage.prefs.BaseSharedPreferences
import com.zar.core.storage.token.TokenStoreEncrypted
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

object CoreModules {
    private val API_HTTP_CLIENT = named("core-network/http-client")
    private val API_NETWORK_CLIENT = named("core-network/network-client")

    fun provide(
        appConfig: AppConfig,
        stringProvider: StringProvider,
        networkStatusMonitor: NetworkStatusMonitor,
        sharedPreferences: BaseSharedPreferences,
        tokenStoreOverride: TokenStore? = null,
        authOrchestrator: AuthOrchestrator? = null,
        cacheDir: File? = null
    ): List<Module> {
        val commonModule = module {
            single { appConfig }
            single<StringProvider> { stringProvider }
            single<NetworkStatusMonitor> { networkStatusMonitor }
        }

        val storageModule = module {
            single<BaseSharedPreferences> { sharedPreferences }
            single<TokenStore> { tokenStoreOverride ?: TokenStoreEncrypted(get()) }
        }

        val networkModule = module {
            single { NetworkConfig(baseUrl = appConfig.baseUrl, loggingEnabled = appConfig.enableLogging) }
            single<HttpCacheRepository> { HttpCacheRepositoryImpl() }
            single { CircuitBreaker(get<NetworkConfig>().circuit) }
            single<ErrorMapper> { ErrorMapperImpl(get()) }
            single(API_HTTP_CLIENT) {
                HttpClientFactory.create(
                    config = get(),
                    tokenStore = get(),
                    authOrchestrator = authOrchestrator,
                    cacheDir = cacheDir
                )
            }
            single(API_NETWORK_CLIENT) {
                NetworkClient(
                    httpClient = get(API_HTTP_CLIENT),
                    statusMonitor = get(),
                    errorMapper = get(),
                    cacheRepository = get(),
                    circuitBreaker = get(),
                    config = get()
                )
            }
            single<RawApi> { get<NetworkClient>(API_NETWORK_CLIENT) }
            single<EnvelopeApi> { get<NetworkClient>(API_NETWORK_CLIENT) }
        }

        return listOf(commonModule, storageModule, networkModule)
    }
}