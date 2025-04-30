package com.msa.core.data.network.model

data class NetworkConfig(
    val connectTimeout: Long = 15_000,
    val socketTimeout: Long = 15_000,
    val requestTimeout: Long = 15_000,
    val maxRetries: Int = 3,
    val cacheSize: Long = 10 * 1024 * 1024, // 10 MB
    val sslPinningEnabled: Boolean = false,
    val baseUrl: String = "https://api.example.com"
) {
    companion object {
        val DEFAULT = NetworkConfig()
    }
}