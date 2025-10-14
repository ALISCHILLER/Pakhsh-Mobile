package com.zar.core.base


import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.result.NetworkResult
import com.zar.core.data.network.result.NetworkResult.Success
import com.zar.core.data.network.result.NetworkResultException
import com.zar.core.data.network.result.map
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

open class BaseRepository protected constructor(
    protected val networkHandler: NetworkHandler,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    // region Suspend wrappers
    suspend inline fun <reified T> get(
        url: String,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.get(url, requireConnection, label, builder)

    suspend inline fun <reified T> post(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.post(url, body, requireConnection, label, builder)

    suspend inline fun <reified T> put(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.put(url, body, requireConnection, label, builder)

    suspend inline fun <reified T> patch(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.patch(url, body, requireConnection, label, builder)

    suspend inline fun <reified T> delete(
        url: String,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.delete(url, requireConnection, label, builder)

    suspend inline fun <reified T> request(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> =
        networkHandler.request(method, url, requireConnection, body, label, builder)

    // --- Flow Helpers ---

    protected inline fun <reified T> requestFlow(
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> T?)? = null,
        crossinline block: suspend NetworkHandler.() -> NetworkResult<T>,
    ): Flow<NetworkResult<T>> = flow {
        if (emitIdleFirst) emit(NetworkResult.Idle)

        cachedValue?.let { loader ->
            val cached = withContext(ioDispatcher) { loader() }
            cached?.let { emit(Success(it)) }
        }

        if (emitLoading) emit(NetworkResult.Loading)

        var attempt = 0L
        var lastSuccess: T? = null
        var hasEmittedSuccess = false
        while (true) {
            val result = withContext(ioDispatcher) { networkHandler.block() }
            val shouldEmit = when {
                result is Success && distinctSuccess && hasEmittedSuccess && lastSuccess == result.data -> false
                else -> true
            }

            if (result is Success) {
                lastSuccess = result.data
                hasEmittedSuccess = true
            }

            if (shouldEmit) emit(result)

            if (result is NetworkResult.Error) {
                val throwable = result.cause ?: NetworkResultException(result.error, result.metadata)
                val shouldRetry = retryPolicy?.invoke(throwable, attempt) == true
                if (shouldRetry) {
                    attempt++
                    continue
                }
            }
            break
        }
    }

    protected inline fun <reified In, reified Out> requestFlow(
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> In?)? = null,
        crossinline block: suspend NetworkHandler.() -> NetworkResult<In>,
        crossinline mapper: (In) -> Out,
    ): Flow<NetworkResult<Out>> {
        return requestFlow(emitLoading, emitIdleFirst, distinctSuccess, retryPolicy, cachedValue, block)
            .map { result -> result.map(mapper) }
    }


    // Convenience shorthands for migrating legacy repositories that expected *AsFlow helpers.

    protected inline fun <reified T> getAsFlow(
        url: String,
        requireConnection: Boolean = true,
        label: String? = null,
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> T?)? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): Flow<NetworkResult<T>> = requestFlow(
        emitLoading = emitLoading,
        emitIdleFirst = emitIdleFirst,
        distinctSuccess = distinctSuccess,
        retryPolicy = retryPolicy,
        cachedValue = cachedValue,
    ) {
        get(url, requireConnection, label, builder)
    }

    protected inline fun <reified T> postAsFlow(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> T?)? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): Flow<NetworkResult<T>> = requestFlow(
        emitLoading = emitLoading,
        emitIdleFirst = emitIdleFirst,
        distinctSuccess = distinctSuccess,
        retryPolicy = retryPolicy,
        cachedValue = cachedValue,
    ) {
        post(url, body, requireConnection, label, builder)
    }

    protected inline fun <reified T> putAsFlow(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        label: String? = null,
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> T?)? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): Flow<NetworkResult<T>> = requestFlow(
        emitLoading = emitLoading,
        emitIdleFirst = emitIdleFirst,
        distinctSuccess = distinctSuccess,
        retryPolicy = retryPolicy,
        cachedValue = cachedValue,
    ) {
        put(url, body, requireConnection, label, builder)
    }

    protected inline fun <reified T> deleteAsFlow(
        url: String,
        requireConnection: Boolean = true,
        label: String? = null,
        emitLoading: Boolean = true,
        emitIdleFirst: Boolean = false,
        distinctSuccess: Boolean = false,
        noinline retryPolicy: (suspend (Throwable, Long) -> Boolean)? = null,
        crossinline cachedValue: (suspend () -> T?)? = null,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): Flow<NetworkResult<T>> = requestFlow(
        emitLoading = emitLoading,
        emitIdleFirst = emitIdleFirst,
        distinctSuccess = distinctSuccess,
        retryPolicy = retryPolicy,
        cachedValue = cachedValue,
    ) {
        delete(url, requireConnection, label, builder)
    }

}