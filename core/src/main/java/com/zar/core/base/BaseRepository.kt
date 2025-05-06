package com.zar.core.base

import com.zar.core.data.network.error.AppError
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.error.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn

/**
 * پایه تمام Repositoryها - مدیریت درخواست‌های شبکه
 */
open class BaseRepository protected constructor(
    open val networkHandler: NetworkHandler
) {

    // --- Suspend Methods ---

    inline suspend fun <reified T> get(url: String): NetworkResult<T> =
        networkHandler.get<T>(url)

    inline suspend fun <reified T> post(url: String, body: Any): NetworkResult<T> =
        networkHandler.post<T>(url, body)

    inline suspend fun <reified T> put(url: String, body: Any): NetworkResult<T> =
        networkHandler.put<T>(url, body)

    inline suspend fun <reified T> delete(url: String): NetworkResult<T> =
        networkHandler.delete<T>(url)

    inline suspend fun <reified T> patch(url: String, body: Any): NetworkResult<T> =
        networkHandler.patch<T>(url, body)

    inline suspend fun <reified T> head(url: String): NetworkResult<T> =
        networkHandler.head<T>(url)


    // --- Flow Methods ---

    inline fun <reified T> getAsFlow(url: String): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.get<T>(url)) }

    inline fun <reified T> postAsFlow(url: String, body: Any): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.post<T>(url, body)) }

    inline fun <reified T> putAsFlow(url: String, body: Any): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.put<T>(url, body)) }

    inline fun <reified T> deleteAsFlow(url: String): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.delete<T>(url)) }

    inline fun <reified T> patchAsFlow(url: String, body: Any): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.patch<T>(url, body)) }

    inline fun <reified T> headAsFlow(url: String): Flow<NetworkResult<T>> =
        flow { emit(networkHandler.head<T>(url)) }
}

    // --- Extension-like Functions ---

    inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is NetworkResult.Success -> NetworkResult.Success(transform(data))
            is NetworkResult.Error -> this
            is NetworkResult.Loading -> this
            is NetworkResult.Idle -> this
        }
    }

    inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Success) action(data)
        return this
    }

    inline fun <T> NetworkResult<T>.onError(action: (AppError) -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Error) action(error)
        return this
    }

    fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Loading) action()
        return this
    }

    fun <T> NetworkResult<T>.onIdle(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Idle) action()
        return this
    }
    fun <T> NetworkResult<T>.onComplete(action: () -> Unit): NetworkResult<T> {
        action()
        return this
    }
    fun <T> NetworkResult<T>.onAny(action: () -> Unit): NetworkResult<T> {
        action()
        return this
    }
    fun <T> NetworkResult<T>.onErrorOrSuccess(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Error || this is NetworkResult.Success) action()
        return this
    }
    fun <T> NetworkResult<T>.onErrorOrLoading(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Error || this is NetworkResult.Loading) action()
        return this
    }
    fun <T> NetworkResult<T>.onErrorOrIdle(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Error || this is NetworkResult.Idle) action()
        return this
    }
    fun <T> NetworkResult<T>.onLoadingOrIdle(action: () -> Unit): NetworkResult<T> {
        if (this is NetworkResult.Loading || this is NetworkResult.Idle) action()
        return this
    }