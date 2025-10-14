package com.zar.core.data.network.model


import com.zar.core.BuildConfig

data class NetworkConfig(
    val connectTimeout: Long = 15_000,
    val socketTimeout: Long = 15_000,
    val requestTimeout: Long = 15_000,
    val maxRetries: Int = 3,
    val cacheSize: Long = 10 * 1024 * 1024, // 10 MB
    val sslPinningEnabled: Boolean = false,
    val baseUrl: String = "https://pokeapi.co/api/v2/",
    val loggingConfig: LoggingConfig = LoggingConfig(),
    val sslConfig: SSLConfig = SSLConfig(),
    val cacheConfig: CacheConfig = CacheConfig(),
    val defaultHeaders: Map<String, String> = emptyMap(),
    val userAgent: String? = null,
) {
    companion object {
        val DEFAULT: NetworkConfig = NetworkConfig()
    }
}

data class LoggingConfig(
    val enabled: Boolean = BuildConfig.DEBUG
)

data class SSLConfig(
    val pinningEnabled: Boolean = false,
    val host: String = "",
    val certificates: List<String> = emptyList(),
)

data class CacheConfig(
    val enabled: Boolean = true,
    val size: Long = 10 * 1024 * 1024 // 10MB
)