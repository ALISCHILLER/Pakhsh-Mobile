package com.msa.core.network.client

import com.msa.core.common.result.Outcome
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.body
import io.ktor.http.HttpMethod

data class NetworkRequest<T>(
    val method: HttpMethod,
    val path: String,
    val query: Map<String, Any?> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val parser: suspend (HttpResponse) -> T
)

interface RawApi {
    suspend fun <T> execute(request: NetworkRequest<T>): Outcome<T>
}

suspend inline fun <reified T> RawApi.get(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(NetworkRequest(HttpMethod.Get, path, query, headers, null) { it.body() })

suspend inline fun <reified T> RawApi.post(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(NetworkRequest(HttpMethod.Post, path, query, headers, body) { it.body() })

suspend inline fun <reified T> RawApi.put(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(NetworkRequest(HttpMethod.Put, path, query, headers, body) { it.body() })

suspend inline fun <reified T> RawApi.delete(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(NetworkRequest(HttpMethod.Delete, path, query, headers, null) { it.body() })