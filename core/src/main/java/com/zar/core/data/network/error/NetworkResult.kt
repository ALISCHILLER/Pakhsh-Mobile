package com.zar.core.data.network.error

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.handler.NetworkException
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber

sealed class NetworkResult<out T> {

    // برای بارگذاری (مثلاً نمایش ProgressBar)
    object Loading : NetworkResult<Nothing>()

    // حالت پیش‌فرض یا اولیه
    object Idle : NetworkResult<Nothing>()

    // موفقیت با داده‌ی موردنظر
    data class Success<out T>(val data: T) : NetworkResult<T>()

    // خطا با جزئیات
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
                return when (exception) {
                    is NetworkException -> {
                        val error = ConnectionError(
                            errorCode = "network_unavailable",
                            message = context.getString(R.string.error_no_connection),
                            connectionType = exception.connectionType?.name ?: "Unknown"
                        )
                        Error(error, attemptedRetries, maxRetries)
                    }

                    is TimeoutCancellationException -> {
                        val error = TimeoutError(
                            errorCode = "request_timeout",
                            message = context.getString(R.string.error_timeout),
                            duration = 30_000,
                            cause = exception
                        )
                        Error(error, attemptedRetries, maxRetries)
                    }

                    else -> {
                        Timber.e(exception, "Unhandled exception in network call")
                        Error(
                            UnknownError(
                                message = context.getString(R.string.error_unknown)
                            ),
                            attemptedRetries,
                            maxRetries
                        )
                    }
                }
            }
        }
    }
}

// Extension Functions

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
