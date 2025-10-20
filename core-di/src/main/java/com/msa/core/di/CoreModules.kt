package com.msa.core.di

import com.msa.core.common.config.AppConfig
import com.msa.core.logging.api.LogContext
import com.msa.core.logging.api.Logger
import com.msa.core.logging.impl.FileLogger
import com.msa.core.network.api.AuthOrchestrator
import com.msa.core.network.api.CircuitBreaker
import com.msa.core.network.api.EnvelopeApi
import com.msa.core.network.api.HttpClientFactory
import com.msa.core.network.api.NetworkStatusMonitor
import com.msa.core.network.api.RawApi
import com.msa.core.network.api.TokenStore as NetworkTokenStore
import com.msa.core.network.impl.EnvelopeApiImpl
import com.msa.core.network.impl.InMemoryHttpClientFactory
import com.msa.core.network.impl.RawApiImpl
import com.msa.core.network.impl.RefreshInterceptor
import com.msa.core.network.impl.ResponseCache
import com.msa.core.network.impl.SimpleCircuitBreaker
import com.msa.core.storage.api.TokenStore as SecureTokenStore
import com.msa.core.storage.impl.InMemorySecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File
import kotlin.time.Duration.Companion.seconds

object CoreModules {
    fun provide(
        cacheDir: File,
        logDir: File,
        baseUrl: String,
        defaultHeaders: Map<String, String> = emptyMap(),
        orchestrator: AuthOrchestrator
    ): Module {
        return module {
            single(createdAtStart = true) { FileLogger(logDir) } bind Logger::class
            single(createdAtStart = true) { (get<Logger>() as FileLogger).withContext(LogContext(traceId = "bootstrap")) }

            single(createdAtStart = true) { InMemorySecureStorage() } bind SecureTokenStore::class
            single(createdAtStart = true) { TokenBridge(get()) } bind NetworkTokenStore::class

            single(createdAtStart = true) { InMemoryHttpClientFactory() } bind HttpClientFactory::class
            single(createdAtStart = true) { SimpleCircuitBreaker() } bind CircuitBreaker::class
            single(createdAtStart = true) { ResponseCache() }
            single(createdAtStart = true) { Mutex() }

            single(createdAtStart = true) { RefreshInterceptor(get(), orchestrator, get()) }
            single<RawApi>(createdAtStart = true) {
                RawApiImpl(
                    httpClient = get<HttpClientFactory>().create(
                        HttpClientConfigFactory.create(baseUrl, cacheDir, defaultHeaders)
                    ),
                    circuitBreaker = get(),
                    refreshInterceptor = get(),
                    cache = get()
                )
            }
            single<EnvelopeApi>(createdAtStart = true) { EnvelopeApiImpl(get()) }

            single<NetworkStatusMonitor>(createdAtStart = true) { SimpleNetworkStatusMonitor() }
        }
    }
}

private object HttpClientConfigFactory {
    fun create(baseUrl: String, cacheDir: File, defaultHeaders: Map<String, String>) = com.msa.core.network.api.HttpClientConfig(
        baseUrl = baseUrl,
        connectTimeout = 10.seconds,
        readTimeout = 10.seconds,
        writeTimeout = 10.seconds,
        enableLogging = true,
        cacheSizeBytes = 512 * 1024,
        cacheDirectory = cacheDir,
        defaultHeaders = defaultHeaders
    )
}

private class TokenBridge(
    private val storage: InMemorySecureStorage
) : NetworkTokenStore {
    override suspend fun read(): com.msa.core.network.api.AuthTokens? {
        return storage.readTokens()?.let {
            com.msa.core.network.api.AuthTokens(it.accessToken, it.refreshToken, it.expiresAtEpochSeconds)
        }
    }

    override suspend fun write(tokens: com.msa.core.network.api.AuthTokens) {
        storage.writeTokens(tokens.accessToken, tokens.refreshToken, tokens.expiresAtEpochSeconds)
    }

    override suspend fun clear() {
        storage.clearTokens()
    }
}

private class SimpleNetworkStatusMonitor : NetworkStatusMonitor {
    private val state = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = state.asStateFlow()
    fun update(connected: Boolean) { state.value = connected }
}