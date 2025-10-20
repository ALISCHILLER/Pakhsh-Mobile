package com.zar.core.data.network.client

import com.zar.core.R
import com.zar.core.data.network.common.StringProvider
import com.msa.core.common.error.AppError
import com.zar.core.data.network.error.ErrorMapper
import com.zar.core.data.network.model.ApiResponse
import com.zar.core.data.network.result.NetworkMetadata
import com.zar.core.data.network.result.NetworkResult
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class NetworkClient(
    // ⬇️ این باید قابل‌دسترس از متدهای inline public باشد
    @PublishedApi
    internal val httpClient: HttpClient,

    private val statusMonitor: NetworkStatusMonitor,
    private val stringProvider: StringProvider,
    private val errorMapper: ErrorMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    @Volatile
    private var lastKnownConnectionType: NetworkStatusMonitor.ConnectionType? = null

    fun hasNetworkConnection(): Boolean =
        statusMonitor.currentStatus() is NetworkStatusMonitor.NetworkStatus.Available

    fun asError(
        throwable: Throwable,
        attemptedRetries: Int = 0,
        maxRetries: Int = 3
    ): NetworkResult.Error {
        val appError = errorMapper.fromThrowable(throwable)
        return NetworkResult.Error(
            error = appError,
            cause = throwable,
            metadata = NetworkMetadata(
                attemptedRetries = attemptedRetries,
                maxRetries = maxRetries,
                message = appError.message,
            ),
        )
    }

    // ---------------- Envelope (ApiResponse<T>) ----------------

    suspend inline fun <reified T> get(
        url: String,
        requireConnection: Boolean = true
    ): NetworkResult<T> = executeEnvelope(requireConnection) {
        httpClient.get(url).body<ApiResponse<T>>()
    }

    suspend inline fun <reified Req, reified Res> post(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeEnvelope(requireConnection) {
        httpClient.post(url) { setBody(body) }.body<ApiResponse<Res>>()
    }

    suspend inline fun <reified Req, reified Res> put(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeEnvelope(requireConnection) {
        httpClient.put(url) { setBody(body) }.body<ApiResponse<Res>>()
    }

    suspend inline fun <reified Req, reified Res> patch(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeEnvelope(requireConnection) {
        httpClient.patch(url) { setBody(body) }.body<ApiResponse<Res>>()
    }

    suspend inline fun <reified T> delete(
        url: String,
        requireConnection: Boolean = true
    ): NetworkResult<T> = executeEnvelope(requireConnection) {
        httpClient.delete(url).body<ApiResponse<T>>()
    }

    // ---------------- HEAD (بدون ApiResponse) ----------------

    suspend fun head(
        url: String,
        requireConnection: Boolean = true
    ): NetworkResult<Unit> {
        val offline = offlineErrorIfNeeded(requireConnection)
        if (offline != null) return offline

        return try {
            val response = withContext(dispatcher) { httpClient.head(url) }
            if (response.status.isSuccess()) {
                NetworkResult.Success(
                    data = Unit,
                    metadata = NetworkMetadata(
                        statusCode = response.status.value,
                        message = response.status.description,
                    ),
                )
            } else {
                val error = errorMapper.fromStatusCode(
                    response.status.value,
                    response.status.description,
                )
                NetworkResult.Error(
                    error = error,
                    metadata = NetworkMetadata(
                        statusCode = response.status.value,
                        message = error.message,
                    ),
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "HEAD request to %s failed", url)
            val error = errorMapper.fromThrowable(throwable)
            NetworkResult.Error(
                error = error,
                cause = throwable,
                metadata = NetworkMetadata(message = error.message),
            )
        }
    }

    // ---------------- Raw (بدون ApiResponse<T>) ----------------

    suspend inline fun <reified T> getRaw(
        url: String,
        requireConnection: Boolean = true
    ): NetworkResult<T> = executeRaw(requireConnection) {
        httpClient.get(url).body<T>()
    }

    suspend inline fun <reified Req, reified Res> postRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeRaw(requireConnection) {
        httpClient.post(url) { setBody(body) }.body<Res>()
    }

    suspend inline fun <reified Req, reified Res> putRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeRaw(requireConnection) {
        httpClient.put(url) { setBody(body) }.body<Res>()
    }

    suspend inline fun <reified Req, reified Res> patchRaw(
        url: String,
        body: Req,
        requireConnection: Boolean = true
    ): NetworkResult<Res> = executeRaw(requireConnection) {
        httpClient.patch(url) { setBody(body) }.body<Res>()
    }

    suspend inline fun <reified T> deleteRaw(
        url: String,
        requireConnection: Boolean = true
    ): NetworkResult<T> = executeRaw(requireConnection) {
        httpClient.delete(url).body<T>()
    }

    // ---------------- اجرای هسته‌ای (با PublishedApi) ----------------

    @PublishedApi
    internal suspend fun <T> executeRaw(
        requireConnection: Boolean,
        block: suspend () -> T
    ): NetworkResult<T> {
        val offline = offlineErrorIfNeeded(requireConnection)
        if (offline != null) return offline

        return try {
            val result = withContext(dispatcher) { block() }
            NetworkResult.Success(
                data = result,
                metadata = NetworkMetadata(message = "OK")
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Raw network call failed")
            val error = errorMapper.fromThrowable(throwable)
            NetworkResult.Error(
                error = error,
                cause = throwable,
                metadata = NetworkMetadata(message = error.message),
            )
        }
    }

    @PublishedApi
    internal suspend fun <T> executeEnvelope(
        requireConnection: Boolean,
        block: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        val offline = offlineErrorIfNeeded(requireConnection)
        if (offline != null) return offline

        return try {
            val response = withContext(dispatcher) { block() }
            response.toResult()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Envelope network call failed")
            val error = errorMapper.fromThrowable(throwable)
            NetworkResult.Error(
                error = error,
                cause = throwable,
                metadata = NetworkMetadata(message = error.message),
            )
        }
    }

    // ---------------- Helpers ----------------

    private fun offlineErrorIfNeeded(requireConnection: Boolean): NetworkResult.Error? {
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
                    metadata = NetworkMetadata(message = error.message),
                )
            }
        }
    }

    private fun <T> ApiResponse<T>.toResult(): NetworkResult<T> {
        val metadata = toMetadata()
        if (!hasError) {
            val payload = data
            return if (payload != null) {
                NetworkResult.Success(payload, metadata)
            } else {
                val error = errorMapper.emptyBody()
                NetworkResult.Error(
                    error = error,
                    metadata = metadata.copy(message = error.message),
                )
            }
        }

        val statusCode = code.takeIf { it != 0 }
        val error: AppError = statusCode?.let {
            errorMapper.fromStatusCode(it, message)
        } ?: AppError.Network(
            message = message.takeIf { !it.isNullOrBlank() }
                ?: stringProvider.get(R.string.error_server_generic),
            errorCode = "api_error"
        )
        return NetworkResult.Error(
            error = error,
            metadata = metadata.copy(
                message = error.message?.takeIf { it.isNotBlank() } ?: metadata.message
            ),
        )
    }

    private fun ApiResponse<*>.toMetadata(): NetworkMetadata = NetworkMetadata(
        statusCode = code.takeIf { it != 0 },
        status = status.takeIf { it.isNotBlank() },
        message = message.takeIf { it.isNotBlank() },
        pagination = pagination,
    )
}
