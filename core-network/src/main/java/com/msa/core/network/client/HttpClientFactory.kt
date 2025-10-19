package com.msa.core.network.client

import com.msa.core.network.auth.AuthOrchestrator
import com.msa.core.network.auth.TokenStore
import com.msa.core.network.config.NetworkConfig
import com.msa.core.network.config.NetHeaders
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.buildString
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import io.ktor.util.getOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CertificatePinner
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object HttpClientFactory {
    private val RefreshAttemptKey = AttributeKey<Boolean>("core-network-refresh-attempt")

    fun create(
        config: NetworkConfig,
        tokenStore: TokenStore?,
        authOrchestrator: AuthOrchestrator?,
        cacheDir: File? = null
    ): HttpClient {
        val refreshMutex = Mutex()
        return HttpClient(OkHttp) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(jsonSerializer())
            }

            install(Logging) {
                level = if (config.loggingEnabled) LogLevel.ALL else LogLevel.NONE
            }

            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeoutMs
                socketTimeoutMillis = config.readTimeoutMs
                requestTimeoutMillis = config.writeTimeoutMs
            }

            if (config.retry.enabled) {
                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = config.retry.maxRetries)
                    retryOnExceptionIf(maxRetries = config.retry.maxRetries) { _, cause ->
                        cause is IOException
                    }
                    retryIf(maxRetries = config.retry.maxRetries) { _, response ->
                        response?.status?.value in config.retry.retryStatusCodes
                    }
                    delayMillis { attempt ->
                        val exponent = (attempt - 1).coerceAtLeast(0)
                        val baseDelay = config.retry.baseDelayMs * (1L shl exponent)
                        val jitter = if (config.retry.jitterMs > 0) {
                            Random.nextLong(0, config.retry.jitterMs + 1)
                        } else {
                            0L
                        }
                        baseDelay + jitter
                    }
                }
            }

            defaultRequest {
                url(config.baseUrl)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                headers.append(NetHeaders.ACCEPT_LANGUAGE, Locale.getDefault().toLanguageTag())
                config.defaultHeaders.forEach { (key, value) -> headers.append(key, value) }
            }

            if (tokenStore != null && authOrchestrator != null) {
                install(HttpSend) {
                    intercept { request ->
                        val requestUrl = request.url.buildString()
                        val shouldAttach = authOrchestrator.shouldAttach(requestUrl)
                        if (shouldAttach) {
                            tokenStore.accessToken()?.let { access ->
                                request.headers.remove(HttpHeaders.Authorization)
                                request.headers.append(HttpHeaders.Authorization, authOrchestrator.authHeader(access))
                            }
                        }

                        val initialCall = execute(request)
                        val alreadyAttempted = request.attributes.getOrNull(RefreshAttemptKey) == true
                        if (!shouldAttach || alreadyAttempted || initialCall.response.status != HttpStatusCode.Unauthorized) {
                            return@intercept initialCall
                        }

                        request.attributes.put(RefreshAttemptKey, true)
                        val refreshed = refreshMutex.withLock {
                            val currentAccess = tokenStore.accessToken()
                            val currentRefresh = tokenStore.refreshToken()
                            authOrchestrator.refresh(currentAccess, currentRefresh)?.also { (access, refresh) ->
                                tokenStore.updateTokens(access, refresh)
                            }
                        }

                        if (refreshed != null) {
                            request.headers.remove(HttpHeaders.Authorization)
                            request.headers.append(HttpHeaders.Authorization, authOrchestrator.authHeader(refreshed.first))
                            return@intercept execute(request)
                        }

                        initialCall
                    }
                }
            }

            engine {
                config {
                    connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
                    readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
                    writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)

                    if (cacheDir != null && config.cache.enabled) {
                        if (!cacheDir.exists()) {
                            cacheDir.mkdirs()
                        }
                        cache(Cache(cacheDir, config.cache.sizeBytes))
                    }

                    if (config.ssl.pinningEnabled && config.ssl.hostPins.isNotEmpty()) {
                        val builder = CertificatePinner.Builder()
                        config.ssl.hostPins.forEach { (host, pins) ->
                            pins.forEach { builder.add(host, it) }
                        }
                        certificatePinner(builder.build())
                    }
                }
            }
        }
    }

    private fun jsonSerializer(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }
}