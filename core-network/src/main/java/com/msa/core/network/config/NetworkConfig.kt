package com.msa.core.network.config

data class NetworkConfig(
    val baseUrl: String,
    val connectTimeoutMs: Long = 15_000,
    val readTimeoutMs: Long = 15_000,
    val writeTimeoutMs: Long = 15_000,
    val loggingEnabled: Boolean = true,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val ssl: SSLConfig = SSLConfig(),
    val cache: CacheConfig = CacheConfig(),
    val retry: RetryPolicy = RetryPolicy(),
    val circuit: CircuitPolicy = CircuitPolicy(),
    val cachePolicy: CachePolicy = CachePolicy.NetworkFirst
)

enum class CachePolicy { NetworkFirst, CacheFirst, OfflineOnly, NoCache }

data class SSLConfig(
    val pinningEnabled: Boolean = false,
    val hostPins: Map<String, List<String>> = emptyMap()
)

data class CacheConfig(
    val enabled: Boolean = true,
    val sizeBytes: Long = 10L * 1024 * 1024
)

data class RetryPolicy(
    val enabled: Boolean = true,
    val maxRetries: Int = 3,
    val baseDelayMs: Long = 300,
    val jitterMs: Long = 150,
    val retryStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504)
)

data class CircuitPolicy(
    val enabled: Boolean = false,
    val failureThreshold: Int = 5,
    val rollingWindowMs: Long = 30_000,
    val halfOpenAfterMs: Long = 60_000
)