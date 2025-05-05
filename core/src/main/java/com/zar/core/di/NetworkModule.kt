package com.zar.core.di

import android.content.Context
import com.zar.core.data.network.handler.NetworkException
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.Cache
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

val networkModule = module {

    single { NetworkConfig.DEFAULT }

    single {
        NetworkStatusMonitor(androidContext())
    }

    single(named("OkHttpCache")) {
        val config = get<NetworkConfig>()
        Cache(androidContext().cacheDir, config.cacheSize)
    }

    // تعریف NetworkHandler به عنوان singleton


    single {
        val config = get<NetworkConfig>()
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                    readTimeout(config.socketTimeout, TimeUnit.MILLISECONDS)
                    writeTimeout(config.requestTimeout, TimeUnit.MILLISECONDS)
                    retryOnConnectionFailure(true)
                    cache(get(named("OkHttpCache")))
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }

            install(HttpCache)

            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeout
                connectTimeoutMillis = config.connectTimeout
                socketTimeoutMillis = config.socketTimeout
            }

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            defaultRequest {
                url(config.baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                headers.append(HttpHeaders.AcceptLanguage, Locale.getDefault().language)
            }

            // مدیریت خطاهای پیش‌فرض
            HttpResponseValidator {
                handleResponseExceptionWithRequest { cause, _ ->
                    if (cause is IOException) {
                        throw NetworkException.fromStatusCode(-1, androidContext(), cause)
                    }
                }
            }
        }
    }
    // Initialize NetworkHandler and return the singleton object
    single {
        NetworkHandler.initialize(
            context = androidContext(),
            monitor = get(),
            config = get()
        )
        NetworkHandler // return the singleton instance
    }

}
