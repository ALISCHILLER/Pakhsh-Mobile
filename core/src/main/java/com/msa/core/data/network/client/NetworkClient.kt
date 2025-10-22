package com.msa.core.data.network.client

import com.msa.core.R
import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.common.error.AppError
import com.msa.core.data.network.common.StringProvider
import com.msa.core.data.network.error.ErrorMapper
import com.msa.core.data.network.model.ApiResponse
import com.msa.core.data.network.result.NetworkMetadata
import com.msa.core.data.network.result.NetworkResult
import com.msa.core.data.network.utils.NetworkStatusMonitor
import com.msa.core.common.time.Clock
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.util.concurrent.TimeUnit


class NetworkClient(
    @PublishedApi internal val httpClient: HttpClient,

    private val statusMonitor: NetworkStatusMonitor,
    private val stringProvider: StringProvider,
    private val errorMapper: ErrorMapper,
    private val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers(),
    private val clock: Clock = Clock.System,
) {

    @Volatile
    private var lastKnownConnectionType: NetworkStatusMonitor.ConnectionType? = null

    fun hasNetworkConnection(): Boolean =
        statusMonitor.currentStatus() is NetworkStatusMonitor.NetworkStatus.Available

    fun asError(
        throwable: Throwable,
        attemptedRetries: Int = 0,
        maxRetries: Int = 3,
        endpoint: String? = null,
        method: String? = null,
    ): NetworkResult.Error {
        val appError = errorMapper.fromThrowable(throwable)
        return NetworkResult.Error(
            error = appError,
            cause = throwable,
            metadata = NetworkMetadata(
                attemptedRetries = attemptedRetries,
                maxRetries = maxRetries,
                message = appError.message,
                retryAfterSeconds = appError.rateLimitSeconds(),
                endpoint = endpoint,
                method = method,
                receivedAtMillis = clock.nowMillis(),
                connectionType = lastKnownConnectionType?.name,
            ),
        )
    }

    // ---------------- Envelope (ApiResponse<T>) ----------------

    suspend inline fun <reified T> get(
        url: String,
        requireConnection: Boolean = true,
    ): NetworkResult<T> = executeEnvelope(
        method = "GET",
        url = url,
        requireConnection = requireConnection,
    ) { httpClient.get(url) }

    suspend inline fun <reified Req, reified Res> post(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeEnvelope(
        method = "POST",
        url = url,
        requireConnection = requireConnection,
    ) { httpClient.post(url) { setBody(body) } }

    suspend inline fun <reified Req, reified Res> put(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeEnvelope(
        method = "PUT",
        url = url,
        requireConnection = requireConnection,
    ) { httpClient.put(url) { setBody(body) } }

    suspend inline fun <reified Req, reified Res> patch(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeEnvelope(
        method = "PATCH",
        url = url,
        requireConnection = requireConnection,
    ) { httpClient.patch(url) { setBody(body) } }

    suspend inline fun <reified T> delete(
        url: String,
        requireConnection: Boolean = true,
    ): NetworkResult<T> = executeEnvelope(
        method = "DELETE",
        url = url,
        requireConnection = requireConnection,
    ) { httpClient.delete(url) }

    // ---------------- HEAD (بدون ApiResponse) ----------------

    suspend fun head(
        url: String,
        requireConnection: Boolean = true,
    ): NetworkResult<Unit> = executeRaw(
        method = "HEAD",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.head(url) },
    ) { Unit }

    // ---------------- Raw (بدون ApiResponse<T>) ----------------

    suspend inline fun <reified T> getRaw(
        url: String,
        requireConnection: Boolean = true,
    ): NetworkResult<T> = executeRaw(
        method = "GET",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.get(url) },
    ) { response -> response.body() }

    suspend inline fun <reified Req, reified Res> postRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeRaw(
        method = "POST",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.post(url) { setBody(body) } },
    ) { response -> response.body() }

    suspend inline fun <reified Req, reified Res> putRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeRaw(
        method = "PUT",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.put(url) { setBody(body) } },
    ) { response -> response.body() }

    suspend inline fun <reified Req, reified Res> patchRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true,
    ): NetworkResult<Res> = executeRaw(
        method = "PATCH",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.patch(url) { setBody(body) } },
    ) { response -> response.body() }

    suspend inline fun <reified T> deleteRaw(
        url: String,
        requireConnection: Boolean = true,
    ): NetworkResult<T> = executeRaw(
        method = "DELETE",
        url = url,
        requireConnection = requireConnection,
        block = { httpClient.delete(url) },
    ) { response -> response.body() }

    // ---------------- اجرای هسته‌ای (با PublishedApi) ----------------

    @PublishedApi
    internal suspend fun <T> executeRaw(
        method: String,
        url: String,
        requireConnection: Boolean,
        block: suspend () -> HttpResponse,
        extractor: suspend (HttpResponse) -> T,
    ): NetworkResult<T> {
        val offline = offlineErrorIfNeeded(requireConnection, url, method)
        if (offline != null) return offline

        return try {
            val (response, durationNanos) = dispatchers.withIo {
                val start = System.nanoTime()
                val httpResponse = block()
                httpResponse to (System.nanoTime() - start)
            }

            val metadata = response.toMetadata(durationNanos, clock, lastKnownConnectionType?.name)
            if (!response.status.isSuccess()) {
                val error = errorMapper.fromStatusCode(
                    statusCode = response.status.value,
                    fallbackMessage = response.status.description,
                    headers = response.headers,
                    endpoint = metadata.endpoint ?: url,
                )
                return NetworkResult.Error(
                    error = error,
                    metadata = metadata.withError(error),
                )
            }

            val payload = dispatchers.withIo { extractor(response) }
            NetworkResult.Success(
                data = payload,
                metadata = metadata.copy(
                    message = metadata.message ?: "OK",
                    connectionType = metadata.connectionType ?: lastKnownConnectionType?.name,
                ),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Raw network call failed for %s %s", method, url)
            val error = errorMapper.fromThrowable(throwable)
            NetworkResult.Error(
                error = error,
                cause = throwable,
                metadata = NetworkMetadata(
                    message = error.message,
                    retryAfterSeconds = error.rateLimitSeconds(),
                    endpoint = url,
                    method = method,
                    receivedAtMillis = clock.nowMillis(),
                    connectionType = lastKnownConnectionType?.name,
                ),
            )
        }
    }

    @PublishedApi
    internal suspend fun <T> executeEnvelope(
        method: String,
        url: String,
        requireConnection: Boolean,
        block: suspend () -> HttpResponse,
    ): NetworkResult<T> {
        val offline = offlineErrorIfNeeded(requireConnection, url, method)
        if (offline != null) return offline

        return try {
            val (response, durationNanos) = dispatchers.withIo {
                val start = System.nanoTime()
                val httpResponse = block()
                httpResponse to (System.nanoTime() - start)
            }

            val baseMetadata = response.toMetadata(durationNanos, clock, lastKnownConnectionType?.name)
            if (!response.status.isSuccess()) {
                val error = errorMapper.fromStatusCode(
                    statusCode = response.status.value,
                    fallbackMessage = response.status.description,
                    headers = response.headers,
                    endpoint = baseMetadata.endpoint ?: url,
                )
                return NetworkResult.Error(
                    error = error,
                    metadata = baseMetadata.withError(error),
                )
            }

            val envelope = dispatchers.withIo { response.body<ApiResponse<T>>() }
            envelope.toResult(baseMetadata)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Envelope network call failed for %s %s", method, url)
            val error = errorMapper.fromThrowable(throwable)
            NetworkResult.Error(
                error = error,
                cause = throwable,
                metadata = NetworkMetadata(
                    message = error.message,
                    retryAfterSeconds = error.rateLimitSeconds(),
                    endpoint = url,
                    method = method,
                    receivedAtMillis = clock.nowMillis(),
                    connectionType = lastKnownConnectionType?.name,
                ),
            )
        }
    }

    // ---------------- Helpers ----------------

    private fun offlineErrorIfNeeded(
        requireConnection: Boolean,
        endpoint: String?,
        method: String?,
    ): NetworkResult.Error? {
        if (!requireConnection) return null
        return when (val status = statusMonitor.currentStatus()) {
            is NetworkStatusMonitor.NetworkStatus.Available -> {
                lastKnownConnectionType = status.connectionType
                null
            }
            NetworkStatusMonitor.NetworkStatus.Unavailable -> {
                val error = errorMapper.noConnection(lastKnownConnectionType)
                NetworkResult.Error(
                    error = error,
                    metadata = NetworkMetadata(
                        message = error.message,
                        retryAfterSeconds = error.rateLimitSeconds(),
                        endpoint = endpoint,
                        method = method,
                        receivedAtMillis = clock.nowMillis(),
                        connectionType = lastKnownConnectionType?.name,
                    ),
                )
            }
        }
    }

    private fun <T> ApiResponse<T>.toResult(baseMetadata: NetworkMetadata): NetworkResult<T> {
        val metadata = baseMetadata.mergeWith(this)
        if (!hasError) {
            val payload = data
            return if (payload != null) {
                NetworkResult.Success(payload, metadata)
            } else {
                val error = errorMapper.emptyBody()
                NetworkResult.Error(
                    error = error,
                    metadata = metadata.copy(message = error.message ?: metadata.message),
                )
            }
        }

        val endpoint = metadata.endpoint.orEmpty()
        val statusCode = code.takeIf { it != 0 } ?: metadata.statusCode
        val error: AppError = statusCode?.let {
            errorMapper.fromStatusCode(it, message, endpoint = endpoint)
        } ?: AppError.Network(
            message = message.takeIf { !it.isNullOrBlank() }
                ?: stringProvider.get(R.string.error_server_generic),
            errorCode = "api_error",
            endpoint = endpoint.ifBlank { null },
        )
        return NetworkResult.Error(
            error = error,
            metadata = metadata
                .withError(error)
                .copy(
                    connectionType = metadata.connectionType ?: lastKnownConnectionType?.name,
                ),
        )
    }

    private fun HttpResponse.toMetadata(
        durationNanos: Long,
        clock: Clock,
        connectionType: String?,
    ): NetworkMetadata {
        val durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos).coerceAtLeast(0L)
        val requestId = headers[REQUEST_ID_HEADER]
            ?: headers["request-id"]
            ?: headers["Request-Id"]
        val headerMap = headers.toFlatMap()
        return NetworkMetadata(
            statusCode = status.value,
            status = status.description.takeIf { it.isNotBlank() },
            message = status.description.takeIf { it.isNotBlank() },
            headers = headerMap.takeIf { it.isNotEmpty() },
            durationMillis = durationMillis,
            endpoint = request.url.toString(),
            method = request.method.value,
            requestId = requestId,
            receivedAtMillis = clock.nowMillis(),
            connectionType = connectionType,
    )
}
    private fun Headers.toFlatMap(): Map<String, String> =
        entries().associate { (key, values) -> key to values.joinToString(separator = ",") }

    private fun NetworkMetadata.mergeWith(envelope: ApiResponse<*>): NetworkMetadata = copy(
        statusCode = envelope.code.takeIf { it != 0 } ?: statusCode,
        status = envelope.status.takeIf { it.isNotBlank() } ?: status,
        message = envelope.message.takeIf { it.isNotBlank() } ?: message,
        pagination = envelope.pagination ?: pagination,
    )

    private fun NetworkMetadata.withError(error: AppError): NetworkMetadata = copy(
        statusCode = when (error) {
            is AppError.Server -> error.statusCode
            is AppError.Network -> error.statusCode ?: statusCode
            is AppError.RateLimited -> error.statusCode
            else -> statusCode
        },
        message = error.message ?: message,
        retryAfterSeconds = error.rateLimitSeconds() ?: retryAfterSeconds,
        endpoint = when (error) {
            is AppError.Server -> error.endpoint.ifBlank { endpoint ?: error.endpoint }
            is AppError.Network -> error.endpoint ?: endpoint
            else -> endpoint
        },
    )

    private fun AppError.rateLimitSeconds(): Long? =
        (this as? AppError.RateLimited)?.retryAfterSeconds

    private const val REQUEST_ID_HEADER = "X-Request-Id"