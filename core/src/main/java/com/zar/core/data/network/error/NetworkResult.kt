package com.zar.core.data.network.error

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.handler.NetworkException
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import java.io.IOException

sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    object Idle : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val error: AppError,
        val retryCount: Int = 0,
        val operation: String = "unknown"
    ) : NetworkResult<Nothing>() {
        companion object {
            fun fromException(
                exception: Throwable,
                context: Context,
                operation: String = "unknown"
            ): Error {
                return when (exception) {
                    is NetworkException -> {
                        val networkError = ConnectionError(
                            message = exception.message ?: "",
                            cause = exception,
                            connectionType = exception.connectionType
                        )
                        Error(networkError, retryCount = exception.retryCount, operation = operation)
                    }
                    is TimeoutCancellationException -> {
                        val timeoutError = ConnectionError(
                            message = context.getString(com.zar.core.R.string.error_timeout),
                            cause = exception,
                            connectionType = null
                        )
                        Error(timeoutError, operation = operation)
                    }
                    else -> {
                        Timber.e(exception, "Unhandled exception")
                        val genericError = ConnectionError(
                            message = context.getString(com.zar.core.R.string.error_unknown),
                            cause = exception,
                            connectionType = null
                        )
                        Error(genericError, operation = operation)
                    }
                }
            }
        }
    }
}