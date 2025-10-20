package com.msa.core.data.network.model

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/**
 * پیکربندی یکپارچه برای تنظیمات شبکه.
 */


data class NetworkConfig(
    val baseUrl: String = "https://example/api/v2/",
    val imageBaseUrl: String = baseUrl,
    val apiVersion: String = "v1",
    val connectTimeoutMillis: Long = 15_000,
    val socketTimeoutMillis: Long = 15_000,
    val requestTimeoutMillis: Long = 15_000,
    val defaultHeaders: Map<String, String> = mapOf(
        HttpHeaders.Accept to ContentType.Application.Json.toString(),
        HttpHeaders.ContentType to ContentType.Application.Json.toString()
    ),
    val defaultPageSize: Int = 20,
    val maxPageSize: Int = 100,
    val messages: NetworkMessages = NetworkMessages(),
    val logging: LoggingConfig = LoggingConfig(),
    val ssl: SSLConfig = SSLConfig(),
    val cache: CacheConfig = CacheConfig(),
    val maxRetries: Int = 3
) {
    companion object { val DEFAULT = NetworkConfig() }
}

data class LoggingConfig(val enabled: Boolean = true)

data class SSLConfig(
    val pinningEnabled: Boolean = false,
    val hostToPins: Map<String, List<String>> = emptyMap()
)

data class CacheConfig(
    val enabled: Boolean = true,
    val sizeBytes: Long = 10L * 1024L * 1024L
)

/** پیام‌های خطای شبکه که از یک منبع واحد کنترل می‌شوند. */
data class NetworkMessages(
    val networkError: String = "خطای شبکه، لطفا اتصال اینترنت خود را بررسی کنید.",
    val serverError: String = "خطای سرور، لطفا دوباره تلاش کنید.",
    val timeoutError: String = "اتصال به سرور قطع شد، لطفا دوباره تلاش کنید.",
    val generalError: String = "عملیات با خطا مواجه شد، لطفا دوباره تلاش کنید.",
    val invalidCredentials: String = "نام کاربری یا رمز عبور اشتباه است.",
    val unexpectedError: String = "خطای غیرمنتظره رخ داده است."
)
