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
import io.ktor.client.request.header
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CertificatePinner
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
        context: Context,
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
                level = if (config.loggingConfig.enabled) LogLevel.ALL else LogLevel.NONE
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
                    sendWithoutRequest { request -> !request.url.encodedPath.contains("token") }
                }
            }

            // ================================
            // Base URL + Header عمومی
            // ================================
            defaultRequest {
                url(config.baseUrl)
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("Accept-Language", Locale.getDefault().language)
            }

            // ================================
            // OkHttp Engine Config
            // ================================
            engine {
                config {
                    connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                    readTimeout(config.socketTimeout, TimeUnit.MILLISECONDS)
                    writeTimeout(config.requestTimeout, TimeUnit.MILLISECONDS)
                    retryOnConnectionFailure(true)

                    // تنظیمات کش
                    if (config.cacheConfig.enabled) {
                        cache(Cache(context.cacheDir, config.cacheConfig.size))
                    }

                    // تنظیمات SSL Pinning
                    if (config.sslConfig.pinningEnabled && config.sslConfig.certificates.isNotEmpty()) {
                        certificatePinner(
                            CertificatePinner.Builder()
                                .apply {
                                    config.sslConfig.certificates.forEach { cert ->
                                        add("api.example.com", cert)
                                    }
                                }
                                .build()
                        )
                    }
                }

                // interceptor برای افزودن توکن به تمام درخواست‌ها
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
        tokenMutex.withLock {
            currentToken = newToken
        }
    }

    /**
     * حذف توکن فعلی
     */
    suspend fun clearToken() {
        tokenMutex.withLock {
            currentToken = null
        }
    }

    /**
     * تعیین توکن اولیه
     */
    suspend fun setInitialToken(token: String?) {
        tokenMutex.withLock {
            currentToken = token
        }
    }

    /**
     * بررسی وجود توکن
     */
    fun hasToken(): Boolean = currentToken != null && currentToken!!.isNotBlank()

    /**
     * دریافت توکن فعلی
     */
    fun getCurrentToken(): String? = currentToken
}
