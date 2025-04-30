package com.msa.core.data.network.handler

import android.content.Context
import com.msa.core.R
import com.msa.core.data.network.model.ApiResponse
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import java.io.IOException

/**
 * کلاس sealed برای مدیریت وضعیت‌های مختلف درخواست‌های شبکه.
 */
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    object Idle : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val exception: Throwable,
        val message: String,
        val httpCode: Int,
        val retryCount: Int
    ) : NetworkResult<Nothing>() {

        val canRetry: Boolean
            get() = exception is NetworkException && exception.errorCode.shouldRetry

        companion object {
            fun fromException(exception: Throwable, context: Context): Error {
                val networkEx = when (exception) {
                    is NetworkException -> exception
                    is IOException -> NetworkException.fromStatusCode(0, context, exception)
                    is TimeoutCancellationException -> NetworkException.fromStatusCode(408, context, exception)
                    else -> {
                        Timber.e(exception, "Unhandled exception occurred: ${exception.message}")
                        NetworkException.fromStatusCode(-1, context, exception)
                    }
                }
                return Error(
                    exception = networkEx,
                    message = networkEx.message ?: context.getString(R.string.error_unknown),
                    httpCode = networkEx.httpStatus,
                    retryCount = networkEx.retryCount
                )
            }
        }
    }
}
