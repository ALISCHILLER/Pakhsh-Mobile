package com.msa.core.network.client

import com.msa.core.common.result.Outcome
import com.msa.core.network.config.CachePolicy
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod

data class NetworkRequest<T>(
    val method: HttpMethod,
    val path: String,
    val query: Map<String, Any?> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val cachePolicyOverride: CachePolicy? = null,
    val parser: suspend (HttpResponse) -> T
)

interface RawApi {
    suspend fun <T> execute(request: NetworkRequest<T>): Outcome<T>
}

suspend inline fun <reified T> RawApi.get(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    NetworkRequest(
        method = HttpMethod.Get,
        path = path,
        query = query,
        headers = headers,
        body = null,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> RawApi.post(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    NetworkRequest(
        method = HttpMethod.Post,
        path = path,
        query = query,
        headers = headers,
        body = body,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> RawApi.put(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    NetworkRequest(
        method = HttpMethod.Put,
        path = path,
        query = query,
        headers = headers,
        body = body,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)

suspend inline fun <reified T> RawApi.delete(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    cachePolicy: CachePolicy? = null
): Outcome<T> = execute(
    NetworkRequest(
        method = HttpMethod.Delete,
        path = path,
        query = query,
        headers = headers,
        body = null,
        cachePolicyOverride = cachePolicy
    ) { it.body() }
)