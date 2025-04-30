package com.msa.core.di

import android.content.Context
import com.msa.core.BuildConfig
import com.msa.core.data.network.handler.NetworkException
import com.msa.core.data.network.handler.NetworkHandler
import com.msa.core.data.network.model.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.io.IOException
import kotlin.math.pow

/**
 * ماژول برای مدیریت وابستگی‌های شبکه.
 */
val NetworkModule = module {

    // 1. اضافه کردن NetworkConfig
    single { NetworkConfig.DEFAULT } // مقادیر پیش‌فرض

    // 2. HttpClient با تنظیمات پیشرفته
    single {
        val config = get<NetworkConfig>() // دریافت تنظیمات پویا
        HttpClient(OkHttp) {
            // a. پارس JSON
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = false
                    }
                )
            }

            // b. لاگ‌گیری
            install(Logging) {
                logger = Logger.DEFAULT
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE // سطح لاگ بر اساس محیط
            }

            // c. مدیریت Timeout
            install(HttpTimeout) {
                connectTimeoutMillis = config.connectTimeout
                socketTimeoutMillis = config.socketTimeout
                requestTimeoutMillis = config.requestTimeout
            }

            // d. Retry Policy با Exponential Backoff
            install(HttpRequestRetry) {
                maxRetries = config.maxRetries
                retryIf { _, cause ->
                    cause is IOException || cause is TimeoutCancellationException
                }
                delayMillis { attempt -> (1000 * 2.0.pow(attempt.toDouble())).toLong() }
            }

            // e. Headerهای مشترک
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            // f. SSL Pinning (اختیاری)
//            engine {
//                if (config.sslPinningEnabled) {
//                    certificatePinner = CertificatePinner.Builder()
//                        .add("api.example.com", "sha256/ABC123...")
//                        .build()
//                }
//            }

            // g. مدیریت Cache
            install(HttpCache) {
                publicStorage(CacheStorage.Unlimited())
            }

            // h. اعتبارسنجی پاسخ‌ها
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status.value !in 200..299) {
                        throw NetworkException.fromStatusCode(response.status.value, get())
                    }
                }
            }
        }
    }

    // 3. NetworkHandler
    single { NetworkHandler }

    // 4. Context (در صورت نیاز)
    single<Context> { androidApplication() }
}