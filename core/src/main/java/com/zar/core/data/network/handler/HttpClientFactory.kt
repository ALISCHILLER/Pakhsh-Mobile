package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.data.network.model.NetworkConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CertificatePinner
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    private var currentToken: String? = null

    /**
     * ساخت HttpClient با تمام تنظیمات لازم برای APIها
     */
    fun create(
        context: Context,
        config: NetworkConfig = NetworkConfig.DEFAULT
    ): Lazy<HttpClient> = lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
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

            defaultRequest {
                url(config.baseUrl)
                header("Accept", "application/json")
                header("Accept-Language", Locale.getDefault().language)
            }

            engine {
                config {
                    connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                    readTimeout(config.socketTimeout, TimeUnit.MILLISECONDS)
                    writeTimeout(config.requestTimeout, TimeUnit.MILLISECONDS)

                    if (config.cacheConfig.enabled) {
                        cache(Cache(context.cacheDir, config.cacheConfig.size))
                    }

//                    if (config.sslConfig.pinningEnabled && config.sslConfig.certificates.isNotEmpty()) {
//                        certificatePinner(
//                            CertificatePinner.Builder().apply {
//                                config.sslConfig.certificates.forEach {
//                                    add("api.example.com", it)
//                                }
//                            }.build()
//                        )
//                    }
                }
            }
        }
    }
}