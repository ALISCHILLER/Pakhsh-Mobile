package com.zar.core.data.network.result


import android.content.Context
import com.zar.core.data.network.common.AndroidStringProvider
import com.zar.core.data.network.common.StringProvider
import com.zar.core.data.network.error.AppError
import com.zar.core.data.network.error.toAppError
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import com.zar.core.data.network.error.UnknownError

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    object Idle : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val error: AppError,
        val attemptedRetries: Int = 0,
        val maxRetries: Int = 3
    ) : NetworkResult<Nothing>() {
        companion object {
            fun fromException(
                exception: Throwable,
                context: Context,
                attemptedRetries: Int = 0,
                maxRetries: Int = 3
            ): NetworkResult<Nothing> {
                if (exception is CancellationException) throw exception
                val strings: StringProvider = AndroidStringProvider(context)
                val appError = exception.toAppError(strings)
                if (appError is UnknownError) Timber.e(exception, "Unhandled exception in network call")
                return Error(appError, attemptedRetries, maxRetries)
            }
        }
    }
}


inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> this
    is NetworkResult.Loading -> this
    is NetworkResult.Idle -> this
}


inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}
inline fun <T> NetworkResult<T>.onError(action: (AppError) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(error)
    return this
}
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}

inline fun <T> NetworkResult<T>.onIdle(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Idle) action()
    return this
}

inline fun <T> NetworkResult<T>.getOrNull(): T? = when (this) {
    is NetworkResult.Success -> data
    else -> null
}

inline fun <T> NetworkResult<T>.getOrElse(onError: (AppError) -> T): T = when (this) {
    is NetworkResult.Success -> data
    is NetworkResult.Error -> onError(error)
    else -> throw IllegalStateException("Value is not available from $this")
}

inline fun <T> NetworkResult<T>.recover(onError: (AppError) -> NetworkResult<T>): NetworkResult<T> = when (this) {
    is NetworkResult.Error -> onError(error)
    else -> this
}

fun <T> NetworkResult<T>.requireValue(): T = when (this) {
    is NetworkResult.Success -> data
    is NetworkResult.Error -> throw IllegalStateException("Network error: $error")
    is NetworkResult.Loading, is NetworkResult.Idle ->
        throw IllegalStateException("Value is not available while state is $this")
}