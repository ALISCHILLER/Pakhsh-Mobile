package com.msa.core.network.impl

import com.msa.core.common.api.AppError
import com.msa.core.common.api.Meta
import com.msa.core.common.api.Outcome
import com.msa.core.common.api.asFailure
import com.msa.core.network.api.AuthOrchestrator
import com.msa.core.network.api.AuthTokens
import com.msa.core.network.api.CachePolicy
import com.msa.core.network.api.CircuitBreaker
import com.msa.core.network.api.EnvelopeApi
import com.msa.core.network.api.EnvelopeResponse
import com.msa.core.network.api.HttpClient
import com.msa.core.network.api.HttpClientConfig
import com.msa.core.network.api.HttpClientFactory
import com.msa.core.network.api.NetworkRequest
import com.msa.core.network.api.NetworkResponse
import com.msa.core.network.api.RawApi
import com.msa.core.network.api.TokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory HTTP client that allows injecting canned responses for tests.
 */
class InMemoryHttpClientFactory : HttpClientFactory {
    override fun create(config: HttpClientConfig): HttpClient = InMemoryHttpClient()
}

class InMemoryHttpClient : HttpClient {
    private val responses = ConcurrentHashMap<String, NetworkResponse>()

    fun enqueue(path: String, response: NetworkResponse) {
        responses[path] = response
    }

    override suspend fun perform(request: NetworkRequest): NetworkResponse {
        return responses[request.path] ?: throw IOException("No response for ${request.path}")
    }

    override fun close() {}
}

class RawApiImpl(
    private val httpClient: HttpClient,
    private val circuitBreaker: CircuitBreaker,
    private val refreshInterceptor: RefreshInterceptor,
    private val cache: ResponseCache
) : RawApi {
    override suspend fun execute(request: NetworkRequest): Outcome<String> {
        val cacheKey = "${request.method}:${request.path}"
        val cached = if (request.cachePolicy != CachePolicy.NETWORK_ONLY) cache.read(cacheKey) else null
        if (cached != null && request.cachePolicy == CachePolicy.CACHE_ONLY) {
            return Outcome.Success(cached.body, cached.meta)
        }

        return circuitBreaker.attempt(cacheKey) {
            refreshInterceptor.withFreshTokens {
                try {
                    val response = httpClient.perform(request)
                    val meta = Meta(statusCode = response.statusCode, extras = response.headers)
                    if (request.method.equals("GET", ignoreCase = true) && request.cachePolicy != CachePolicy.NETWORK_ONLY) {
                        cache.write(cacheKey, response)
                    }
                    Outcome.Success(response.body, meta)
                } catch (auth: AuthenticationRequiredException) {
                    AppError.Authentication(auth.message ?: "Auth required").asFailure()
                } catch (io: IOException) {
                    if (cached != null && request.cachePolicy == CachePolicy.CACHE_FIRST) {
                        Outcome.Success(cached.body, cached.meta.copy(message = "cache"))
                    } else {
                        AppError.Network(io.message ?: "IO error", io).asFailure()
                    }
                }
            }
        }
    }
}

class EnvelopeApiImpl(
    private val rawApi: RawApi
) : EnvelopeApi {
    override suspend fun <T> execute(
        request: NetworkRequest,
        deserialize: suspend (String) -> T
    ): Outcome<EnvelopeResponse<T>> = when (val raw = rawApi.execute(request)) {
        is Outcome.Success -> try {
            val payload = deserialize(raw.value)
            Outcome.Success(EnvelopeResponse(payload, raw.meta), raw.meta)
        } catch (ex: Exception) {
            AppError.Unknown(ex.message ?: "Parse error", ex).asFailure()
        }
        is Outcome.Failure -> raw
    }
}

class RefreshInterceptor(
    private val tokenStore: TokenStore,
    private val orchestrator: AuthOrchestrator,
    private val mutex: Mutex = Mutex()
) {
    suspend fun <T> withFreshTokens(block: suspend () -> Outcome<T>): Outcome<T> {
        val result = block()
        if (result is Outcome.Failure && result.error is AppError.Authentication) {
            val current = tokenStore.read() ?: return result
            return mutex.withLock {
                val refreshed = orchestrator.refresh(current)
                if (refreshed is Outcome.Success) {
                    tokenStore.write(refreshed.value)
                    block()
                } else {
                    refreshed
                }
            }
        }
        return result
    }
}

class ResponseCache {
    private val store = ConcurrentHashMap<String, CachedResponse>()

    fun read(key: String): CachedResponse? = store[key]
    fun write(key: String, response: NetworkResponse) {
        store[key] = CachedResponse(
            body = response.body,
            meta = Meta(statusCode = response.statusCode, extras = response.headers)
        )
    }
}

data class CachedResponse(val body: String, val meta: Meta)

class SimpleCircuitBreaker(
    private val failureThreshold: Int = 5
) : CircuitBreaker {
    private val failures = ConcurrentHashMap<String, Int>()

    override suspend fun <T> attempt(key: String, block: suspend () -> Outcome<T>): Outcome<T> {
        val failureCount = failures[key] ?: 0
        if (failureCount >= failureThreshold) {
            return AppError.Network("Circuit open for $key").asFailure()
        }
        val result = block()
        if (result is Outcome.Failure) {
            failures[key] = failureCount + 1
        } else {
            failures.remove(key)
        }
        return result
    }
}

class AuthenticationRequiredException(message: String? = null) : RuntimeException(message)

class InMemoryTokenStore : TokenStore {
    private val mutex = Mutex()
    private var tokens: AuthTokens? = null

    override suspend fun read(): AuthTokens? = mutex.withLock { tokens }

    override suspend fun write(tokens: AuthTokens) {
        mutex.withLock { this.tokens = tokens }
    }

    override suspend fun clear() {
        mutex.withLock { tokens = null }
    }
}