package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.data.network.model.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import okhttp3.Cache
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit


/**
 * Factory responsible for constructing a single [HttpClient] instance that is shared across the
 * application. All configuration (timeouts, logging, caching …) is centralized here to guarantee
 * consistent behaviour for every network request.
 */
object HttpClientFactory {

    private var currentToken: String? = null

    /**
     * Allows callers (e.g. after a refresh token flow) to update the Authorization header that will
     * be attached to every subsequent request.
     */
    fun updateAuthToken(token: String?) {
        currentToken = token
    }

    /**
     * Builds a configured [HttpClient]. The client is cheap to create but expensive to configure, so
     * DI should expose it as a singleton.
     */
    fun create(
        context: Context,
        config: NetworkConfig = NetworkConfig.DEFAULT,
        additionalConfigurator: (HttpRequestBuilder.() -> Unit)? = null,
    ): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("Network").v(message)
                    }
                }
                level = if (config.loggingConfig.enabled) LogLevel.ALL else LogLevel.NONE
            }

            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeout
                socketTimeoutMillis = config.socketTimeout
                requestTimeoutMillis = config.requestTimeout
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = config.maxRetries)
                retryOnExceptionIf { _, cause ->
                    cause is IOException || cause is TimeoutCancellationException
                }
                exponentialDelay()
            }
            defaultRequest {
                url.takeFrom(config.baseUrl)
                header(HttpHeaders.Accept, "application/json")
                header("Accept-Language", Locale.getDefault().language)
                currentToken?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                additionalConfigurator?.invoke(this)
            }

            engine {
                config {
                    connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                    readTimeout(config.socketTimeout, TimeUnit.MILLISECONDS)
                    writeTimeout(config.requestTimeout, TimeUnit.MILLISECONDS)

                    if (config.cacheConfig.enabled) {
                        cache(Cache(context.cacheDir, config.cacheConfig.size))
                    }
                }
            }
        }
    }
}