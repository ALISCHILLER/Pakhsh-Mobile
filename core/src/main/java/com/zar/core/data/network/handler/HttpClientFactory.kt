package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.data.network.model.NetworkConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.cache.*
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    private var currentToken: String? = null
    private val tokenMutex = Mutex()

    /**
     * ساخت HttpClient با تمام تنظیمات لازم برای APIها
     */
    fun create(
        baseUrl: String,
        config: NetworkConfig = NetworkConfig.DEFAULT
    ): Lazy<HttpClient> = lazy {
        HttpClient(OkHttp) {
            // ================================
            // JSON Serialization
            // ================================
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // ================================
            // Logging با Timber
            // ================================
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("Network").v(message)
                    }
                }
                level = LogLevel.ALL
            }

            // ================================
            // Timeout ها
            // ================================
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeout
                socketTimeoutMillis = config.socketTimeout
                requestTimeoutMillis = config.requestTimeout
            }

            // ================================
            // Auth Bearer Token
            // ================================
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            accessToken = currentToken.orEmpty(),
                            refreshToken = ""
                        )
                    }
                    sendWithoutRequest { request ->
                        !request.url.encodedPath.contains("token")
                    }
                }
            }

            // ================================
            // HTTP Cache
            // ================================
            install(HttpCache)

            // ================================
            // Base URL
            // ================================
            defaultRequest {
                url(baseUrl)
            }

            // ================================
            // Header های عمومی
            // ================================
            engine {
                addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Accept-Language", Locale.getDefault().language)
                        .apply {
                            currentToken?.let {
                                header("Authorization", "Bearer $it")
                            }
                        }
                    chain.proceed(request.build())
                }
            }
        }
    }

    /**
     * به‌روزرسانی توکن برای Auth Bearer
     */
    suspend fun updateToken(newToken: String?) {
        tokenMutex.lock()
        try {
            currentToken = newToken
        } finally {
            tokenMutex.unlock()
        }
    }

    /**
     * حذف توکن فعلی
     */
    suspend fun clearToken() {
        tokenMutex.lock()
        try {
            currentToken = null
        } finally {
            tokenMutex.unlock()
        }
    }
}