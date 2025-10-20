package com.msa.core.data.network.client

import android.content.Context
import com.msa.core.data.network.auth.AuthConfig
import com.msa.core.data.network.model.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CertificatePinner
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    @Volatile
    private var currentToken: String? = null

    fun setToken(token: String?) {
        currentToken = token
    }

    fun clearToken() {
        currentToken = null
    }

    fun create(
        context: Context,
        config: NetworkConfig = NetworkConfig.DEFAULT,
        auth: AuthConfig? = null
    ): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false

        install(ContentNegotiation) { json(jsonSerializer()) }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = Timber.tag("Network").v(message)
            }
            level = if (config.logging.enabled) LogLevel.ALL else LogLevel.NONE
        }

        install(HttpTimeout) {
            connectTimeoutMillis = config.connectTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
            requestTimeoutMillis = config.requestTimeoutMillis
        }

        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = config.maxRetries)
            exponentialDelay()
        }

        if (auth != null) {
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = auth.tokenStore.accessToken()
                        val refresh = auth.tokenStore.refreshToken()
                        access?.let { BearerTokens(it, refresh.orEmpty()) }
                    }
                    refreshTokens {
                        val oldAccess = auth.tokenStore.accessToken()
                        val oldRefresh = auth.tokenStore.refreshToken()
                        val newPair = auth.refresh(oldAccess, oldRefresh)
                        if (newPair != null) {
                            auth.tokenStore.updateTokens(newPair)
                            BearerTokens(newPair.accessToken, newPair.refreshToken.orEmpty())
                        } else {
                            auth.tokenStore.clear()
                            null
                        }
                    }
                    sendWithoutRequest { true }
                }
            }
        }

        defaultRequest {
            url { takeFrom(config.baseUrl) }
            config.defaultHeaders.forEach { (key, value) ->
                headers.remove(key)
                headers.append(key, value)
            }
            headers.append(HttpHeaders.AcceptLanguage, Locale.getDefault().toLanguageTag())

            if (auth == null) {
                currentToken?.takeIf { it.isNotBlank() }?.let {
                    headers.append(HttpHeaders.Authorization, "Bearer $it")
                }
            }
        }

        engine {
            config {
                connectTimeout(config.connectTimeoutMillis, TimeUnit.MILLISECONDS)
                readTimeout(config.socketTimeoutMillis, TimeUnit.MILLISECONDS)
                writeTimeout(config.requestTimeoutMillis, TimeUnit.MILLISECONDS)

                if (config.cache.enabled) {
                    cache(Cache(context.cacheDir, config.cache.sizeBytes))
                }
                if (config.ssl.pinningEnabled && config.ssl.hostToPins.isNotEmpty()) {
                    val pinner = CertificatePinner.Builder().apply {
                        config.ssl.hostToPins.forEach { (host, pins) ->
                            pins.forEach { add(host, it) }
                        }
                    }.build()
                    certificatePinner(pinner)
                }
            }
        }
    }

    private fun jsonSerializer(): Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}