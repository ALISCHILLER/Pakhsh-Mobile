package com.msa.core.di

import com.msa.core.data.network.handler.NetworkHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * ماژول برای مدیریت وابستگی‌های شبکه.
 */
val NetworkModule = module {

    // HttpClient با تنظیمات پیشرفته
    single {
        HttpClient(OkHttp) {
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

            // لاگ‌گیری
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            // Headerهای مشترک
            defaultRequest {
                contentType(io.ktor.http.ContentType.Application.Json)
                accept(io.ktor.http.ContentType.Application.Json)
            }
        }
    }

    // NetworkHandler
    single { NetworkHandler }
}