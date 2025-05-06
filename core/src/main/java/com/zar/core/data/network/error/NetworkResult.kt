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
    data class Error(val error: AppError) : NetworkResult<Nothing>() {

        companion object {
            fun fromException(exception: Throwable, context: Context): NetworkResult<Nothing> {
                return when (exception) {
                    is NetworkException -> {
                        val error = ConnectionError(
                            errorCode = "network_unavailable",
                            message = context.getString(R.string.error_no_connection),
                            connectionType = exception.connectionType.toString()
                        )
                        Error(error)
                    }

                    is TimeoutCancellationException -> {
                        val error = TimeoutError(
                            errorCode = "request_timeout",
                            message = context.getString(R.string.error_timeout),
                            duration = 30_000,
                            cause = exception
                        )
                        Error(error)
                    }

                    else -> {
                        Timber.e(exception, "Unhandled exception in network call")
                        Error(
                            UnknownError(
                                message = context.getString(R.string.error_unknown)
                            )
                        )
                    }
                }
            }
        }
    }
}
