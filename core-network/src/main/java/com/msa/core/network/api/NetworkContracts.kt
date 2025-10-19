package com.msa.core.network.api

import com.msa.core.common.api.Meta
import com.msa.core.common.api.Outcome
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

enum class CachePolicy {
    CACHE_ONLY,
    CACHE_FIRST,
    NETWORK_ONLY
}

data class NetworkRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val cachePolicy: CachePolicy = CachePolicy.NETWORK_ONLY
)

data class EnvelopeRequest<Payload>(
    val request: NetworkRequest,
    val serializer: suspend (Payload) -> String
)

data class EnvelopeResponse<T>(
    val payload: T,
    val meta: Meta
)

interface RawApi {
    suspend fun execute(request: NetworkRequest): Outcome<String>
}

interface EnvelopeApi {
    suspend fun <T> execute(
        request: NetworkRequest,
        deserialize: suspend (String) -> T
    ): Outcome<EnvelopeResponse<T>>
}

interface TokenStore {
    suspend fun read(): AuthTokens?
    suspend fun write(tokens: AuthTokens)
    suspend fun clear()
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long
)

interface AuthOrchestrator {
    suspend fun refresh(current: AuthTokens): Outcome<AuthTokens>
}

interface NetworkStatusMonitor {
    val isConnected: Flow<Boolean>
}

interface HttpClientFactory {
    fun create(config: HttpClientConfig): HttpClient
}

data class HttpClientConfig(
    val baseUrl: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
    val writeTimeout: Duration,
    val enableLogging: Boolean,
    val cacheSizeBytes: Long,
    val cacheDirectory: java.io.File,
    val defaultHeaders: Map<String, String> = emptyMap()
)

interface HttpClient {
    suspend fun perform(request: NetworkRequest): NetworkResponse
    fun close()
}

data class NetworkResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String
)

interface CircuitBreaker {
    suspend fun <T> attempt(key: String, block: suspend () -> Outcome<T>): Outcome<T>
}