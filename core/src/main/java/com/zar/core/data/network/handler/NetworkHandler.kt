package com.zar.core.data.network.handler

import com.zar.core.data.network.error.NetworkErrorMapper
import com.zar.core.data.network.error.NetworkMetadata
import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.error.NetworkResult.Error
import com.zar.core.data.network.error.NetworkResult.Success
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Headers
import io.ktor.http.takeFrom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
/**
 * Centralised entry point for executing network requests with shared error handling, metadata
 * capture and connectivity awareness.
 */
class NetworkHandler(
    private val client: HttpClient,
    private val config: NetworkConfig,
    private val networkMonitor: NetworkStatusMonitor,
    private val errorMapper: NetworkErrorMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {


    suspend inline fun <reified T> get(
        endpoint: String,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = HttpMethod.Get,
        endpoint = endpoint,
        requireConnection = requireConnection,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = HttpMethod.Post,
        endpoint = endpoint,
        requireConnection = requireConnection,
        requestBody = body,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified T> put(
        endpoint: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = HttpMethod.Put,
        endpoint = endpoint,
        requireConnection = requireConnection,
        requestBody = body,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified T> patch(
        endpoint: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = HttpMethod.Patch,
        endpoint = endpoint,
        requireConnection = requireConnection,
        requestBody = body,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified T> delete(
        endpoint: String,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = HttpMethod.Delete,
        endpoint = endpoint,
        requireConnection = requireConnection,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified T> request(
        method: HttpMethod,
        endpoint: String,
        requireConnection: Boolean = true,
        body: Any? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = execute(
        method = method,
        endpoint = endpoint,
        requireConnection = requireConnection,
        requestBody = body,
        builder = builder,
    ) { response -> response.body() }

    suspend inline fun <reified Raw, reified Output> request(
        method: HttpMethod,
        endpoint: String,
        requireConnection: Boolean = true,
        body: Any? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
        crossinline transform: (Raw) -> Output,
    ): NetworkResult<Output> {
        return when (
            val result = execute(
                method = method,
                endpoint = endpoint,
                requireConnection = requireConnection,
                requestBody = body,
                builder = builder,
            ) { response -> response.body<Raw>() }
        ) {
            is Success -> Success(transform(result.data), result.metadata)
            is Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
            NetworkResult.Idle -> NetworkResult.Idle
        }
    }

    suspend inline fun <reified T> safeApiCall(
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline apiCall: suspend () -> HttpResponse,
        crossinline parser: suspend (HttpResponse) -> T,
    ): NetworkResult<T> {
        val previousStatus = networkMonitor.lastKnownStatus()
        val previousConnection = previousStatus as? NetworkStatusMonitor.NetworkStatus.Available
        val refreshedStatus = if (requireConnection) {
            networkMonitor.refreshStatus()
        } else {
            previousStatus
        }
        val currentConnection = refreshedStatus as? NetworkStatusMonitor.NetworkStatus.Available
        val effectiveConnection = currentConnection ?: previousConnection
        val connectionType = effectiveConnection?.connectionType
        val connectionTypeName = connectionType?.name

        if (requireConnection && currentConnection == null) {
            Timber.w("Skipping request. No active network for %s", label ?: "request")
            return NetworkResult.Error(
                error = errorMapper.noConnection(previousConnection?.connectionType),
                metadata = NetworkMetadata(
                    requestLabel = label,
                    connectionType = connectionTypeName,
                ),
            )
        }

        return try {
            label?.let { Timber.d("Performing API call: %s", it) }
            val response = withContext(dispatcher) { apiCall() }
            val metadata = response
                .toMetadata(label)
                .copy(connectionType = connectionTypeName)
            val payload = withContext(dispatcher) { parser(response) }


            Success(payload, metadata)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            Timber.e(throwable, "API call failed for %s", label ?: "request")
            val errorMetadata = throwable.toErrorMetadata(label, connectionTypeName)
            Error(
                error = errorMapper.map(throwable),
                cause = throwable,
                metadata = errorMetadata,
            )
        }
    }

    private suspend inline fun <reified T> execute(
        method: HttpMethod,
        endpoint: String,
        requireConnection: Boolean,
        requestBody: Any? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit,
        crossinline parser: suspend (HttpResponse) -> T,
    ): NetworkResult<T> {
        return safeApiCall(
            requireConnection = requireConnection,
            label = "${method.value} $endpoint",
            apiCall = {
                client.request {
                    this.method = method
                    applyEndpoint(endpoint)
                    if (requestBody != null) {
                        setBody(requestBody)
                    }
                    builder()
                }
            },
            parser = parser,
        )
    }

    private fun HttpRequestBuilder.applyEndpoint(endpoint: String) {
        val target = when {
            endpoint.isBlank() -> config.baseUrl
            endpoint.startsWith("http://") || endpoint.startsWith("https://") -> endpoint
            else -> buildString {
                append(config.baseUrl.trimEnd('/'))
                append('/')
                append(endpoint.trimStart('/'))
            }
        }
        url.takeFrom(target)
    }

    private fun HttpResponse.toMetadata(label: String?): NetworkMetadata {
        return NetworkMetadata(
            statusCode = status.value,
            headers = headers.toMap(),
            requestMethod = request.method.value,
            requestUrl = request.url.toString(),
            requestLabel = label,
        )
    }

    private fun Throwable.toErrorMetadata(
        label: String?,
        connectionType: String?,
    ): NetworkMetadata {
        val response = (this as? ResponseException)?.response
        return if (response != null) {
            NetworkMetadata(
                statusCode = response.status.value,
                headers = response.headers.toMap(),
                requestMethod = response.request.method.value,
                requestUrl = response.request.url.toString(),
                requestLabel = label,
                connectionType = connectionType,
            )
        } else {
            NetworkMetadata(
                requestLabel = label,
                connectionType = connectionType,
            )
        }
    }

    private fun Headers.toMap(): Map<String, List<String>> =
        names().associateWith { name -> getAll(name) ?: emptyList() }
}