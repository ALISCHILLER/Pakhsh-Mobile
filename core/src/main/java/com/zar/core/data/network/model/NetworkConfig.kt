package com.zar.core.data.network.model

data class NetworkConfig(
    val connectTimeout: Long = 15_000,
    val socketTimeout: Long = 15_000,
    val requestTimeout: Long = 15_000,
    val maxRetries: Int = 3,
    val baseUrl: String = "https://pokeapi.co/api/v2/",
    val logging: LoggingConfig = LoggingConfig(),
    val ssl: SSLConfig = SSLConfig(),
    val cache: CacheConfig = CacheConfig()
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
