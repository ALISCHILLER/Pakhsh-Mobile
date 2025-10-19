package com.msa.core.network.client

import com.msa.core.common.result.Outcome
import com.msa.core.network.envelope.ApiResponse
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.body
import io.ktor.http.HttpMethod

data class EnvelopeRequest<T>(
    val method: HttpMethod,
    val path: String,
    val query: Map<String, Any?> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val parser: suspend (HttpResponse) -> ApiResponse<T>
)

interface EnvelopeApi {
    suspend fun <T> execute(request: EnvelopeRequest<T>): Outcome<T>
}

suspend inline fun <reified T> EnvelopeApi.get(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(EnvelopeRequest(HttpMethod.Get, path, query, headers) { it.body() })

suspend inline fun <reified T> EnvelopeApi.post(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(EnvelopeRequest(HttpMethod.Post, path, query, headers, body) { it.body() })

suspend inline fun <reified T> EnvelopeApi.put(
    path: String,
    body: Any?,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(EnvelopeRequest(HttpMethod.Put, path, query, headers, body) { it.body() })

suspend inline fun <reified T> EnvelopeApi.delete(
    path: String,
    query: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Outcome<T> = execute(EnvelopeRequest(HttpMethod.Delete, path, query, headers) { it.body() })