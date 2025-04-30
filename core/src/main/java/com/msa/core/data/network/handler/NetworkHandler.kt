package com.msa.core.data.network.handler

import android.content.Context
import com.msa.core.data.network.model.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import kotlin.math.pow
import io.ktor.client.plugins.cache.* // اضافه کنید
import io.ktor.client.plugins.cache.storage.* // برای HttpCacheStorage
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.cache.storage.CacheStorage
import java.util.Locale
import io.ktor.client.plugins.cache.HttpCache
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object NetworkHandler {

    private lateinit var appContext: Context
    private val tokenMutex = Mutex()
    private var config = NetworkConfig.DEFAULT
    private var authToken: String? = null // ✅ متغیر ذخیره توکن

    fun initialize(context: Context, config: NetworkConfig = NetworkConfig.DEFAULT) {
        appContext = context.applicationContext
        this.config = config // ذخیره تنظیمات
    }

    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            // 1. پیکربندی JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // 2. لاگ‌گیری ساختارمند
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        when {
                            message.startsWith("{") || message.startsWith("[") ->
                                Timber.tag("ResponseBody").v(message)

                            message.startsWith("HTTP/") ->
                                Timber.tag("HTTP_Status").d(message)

                            else ->
                                Timber.tag("Network").v(message)
                        }
                    }
                }
                level = LogLevel.ALL
            }

            // 3. مدیریت Timeout با مقادیر پیکربندی
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeout
                socketTimeoutMillis = config.socketTimeout
                requestTimeoutMillis = config.requestTimeout
            }

            // 4. Retry Policy با Exponential Backoff (فقط برای خطاهای قابل تلاش)
            install(HttpRequestRetry) {
                maxRetries = config.maxRetries
                retryIf { _, cause ->
                    when {
                        cause is NetworkException -> cause.isRetryable()
                        cause is IOException -> true // خطاهای شبکه عمومی
                        cause is TimeoutCancellationException -> true // خطاهای تایم‌آوت
                        else -> false
                    }
                }
                delayMillis { attempt -> (1000 * 2.0.pow(attempt.toDouble())).toLong() }
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            accessToken = authToken ?: "",
                            refreshToken = "" // اگر از ریفرش توکن استفاده می‌کنید
                        )
                    }
                    sendWithoutRequest { request ->
                        // توکن را در همه درخواست‌ها ارسال کن
                        !request.url.encodedPath.contains("token") // جلوگیری از ارسال توکن در درخواست‌های مربوط به توکن
                    }
                }
            }
            install(HttpCache) {
                publicStorage(CacheStorage.Unlimited()) // ✅ استفاده از CacheStorage جدید
            }
            engine {
                addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val newRequest = originalRequest.newBuilder()
                        .header("Accept-Language", appContext.getLanguageCode())
                        .apply {
                            authToken?.let { token ->
                                header("Authorization", "Bearer $token") // استفاده از header به جای addHeader
                            }
                        }
                        .build()
                    chain.proceed(newRequest) // فقط یک بار proceed فراخوانی شود
                }
                // CertificatePinner.Builder()
                //     .add("api.example.com", "sha256/ABC123...")
                //     .build()
            }

            // 9. تبدیل کدهای HTTP به استثنا
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status.value !in 200..299) {
                        throw NetworkException.fromStatusCode(response.status.value, appContext)
                    }
                }
            }
        }
    }

    /**
     * تابع مرکزی برای انجام درخواست‌های امن
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        require(::appContext.isInitialized) { "NetworkHandler.initialize() must be called first" }

        return try {
            withContext(Dispatchers.IO) {
                NetworkResult.Success(apiCall())
            }
        } catch (e: Exception) {
            Timber.e(e, "API call failed after retries")
            NetworkResult.Error.fromException(e, appContext)
        }
    }

    // متدهای HTTP پوشش داده شده
    suspend inline fun <reified T> get(url: String) = safeApiCall { client.get(url).body<T>() }
    suspend inline fun <reified T> post(url: String, body: Any) =
        safeApiCall { client.post(url) { setBody(body) }.body<T>() }

    suspend inline fun <reified T> put(url: String, body: Any) =
        safeApiCall { client.put(url) { setBody(body) }.body<T>() }

    suspend inline fun <reified T> delete(url: String) =
        safeApiCall { client.delete(url).body<T>() }

    suspend inline fun <reified T> patch(url: String, body: Any) =
        safeApiCall { client.patch(url) { setBody(body) }.body<T>() }

    suspend inline fun <reified T> head(url: String) = safeApiCall { client.head(url).body<T>() }
}


fun Context.getLanguageCode(): String {
    return Locale.getDefault().language // مثال: "fa" یا "en"
}