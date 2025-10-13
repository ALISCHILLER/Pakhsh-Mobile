package com.zar.core.base


import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * پایه تمام Repositoryها - مدیریت درخواست‌های شبکه
 */
open class BaseRepository protected constructor(
    protected val networkHandler: NetworkHandler,
) {

    // --- Suspend Helpers ---

    suspend inline fun <reified T> get(
        url: String,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.get(url, requireConnection, builder)

    suspend inline fun <reified T> post(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.post(url, body, requireConnection, builder)

    suspend inline fun <reified T> put(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.put(url, body, requireConnection, builder)

    suspend inline fun <reified T> patch(
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.patch(url, body, requireConnection, builder)

    suspend inline fun <reified T> delete(
        url: String,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.delete(url, requireConnection, builder)

    suspend inline fun <reified T> request(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        requireConnection: Boolean = true,
        crossinline builder: HttpRequestBuilder.() -> Unit = {},
    ): NetworkResult<T> = networkHandler.request(method, url, requireConnection, body, builder)

    // --- Flow Helpers ---

    protected inline fun <reified T> requestFlow(
        crossinline block: suspend NetworkHandler.() -> NetworkResult<T>,
    ): Flow<NetworkResult<T>> = flow {
        emit(NetworkResult.Loading)
        emit(networkHandler.block())

    }

}