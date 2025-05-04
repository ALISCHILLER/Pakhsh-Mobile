package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.R
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import java.io.IOException

/**
 * کلاس sealed برای مدیریت وضعیت‌های مختلف درخواست‌های شبکه.
 */
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    object Idle : NetworkResult<Nothing>()
    data class Success<out T>(val data: T, val step: String? = null) : NetworkResult<T>()

    // استفاده از یک کلاس Error یکپارچه برای همه اطلاعات خطا
    data class Error(
        val exception: Throwable,
        val message: String,
        val httpCode: Int,
        val retryCount: Int,
        val step: String? = null, // در صورت نیاز به گام‌ها می‌توانید این را اضافه کنید
        val canRetry: Boolean = exception is NetworkException && exception.errorCode.shouldRetry
    ) : NetworkResult<Nothing>() {

        companion object {
            fun fromException(exception: Throwable, context: Context, step: String? = null): Error {
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
                    retryCount = networkEx.retryCount,
                    step = step // اگر گام‌ها را نیاز دارید، اینجا استفاده کنید
                )
            }
        }
    }
}

