package com.msa.core.network.client

import com.msa.core.common.result.Outcome
import com.msa.core.network.envelope.ApiResponse
import io.ktor.client.statement.HttpResponse
import com.msa.core.network.config.CachePolicy
import io.ktor.client.call.body
import io.ktor.http.HttpMethod

data class EnvelopeRequest<T>(
    val method: HttpMethod,
    val path: String,
    val query: Map<String, Any?> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val cachePolicyOverride: CachePolicy? = null,
    val parser: suspend (HttpResponse) -> ApiResponse<T>
)

interface EnvelopeApi {
    suspend fun <T> execute(request: EnvelopeRequest<T>): Outcome<T>
}

suspend inline fun <reified T> EnvelopeApi.get(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    EnvelopeRequest(
        method = HttpMethod.Get,
        path = path,
        query = query,
        headers = headers,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> EnvelopeApi.post(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    EnvelopeRequest(
        method = HttpMethod.Post,
        path = path,
        query = query,
        headers = headers,
        body = body,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> EnvelopeApi.put(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    EnvelopeRequest(
        method = HttpMethod.Put,
        path = path,
        query = query,
        headers = headers,
        body = body,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> EnvelopeApi.delete(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    EnvelopeRequest(
        method = HttpMethod.Delete,
        path = path,
        query = query,
        headers = headers,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)