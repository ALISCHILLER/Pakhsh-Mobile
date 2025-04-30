package com.msa.core.data.network.handler

import android.content.Context
import com.msa.core.R
import com.msa.core.data.network.model.ApiResponse
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import java.io.IOException

/**
 * کلاس مرکزی مدیریت خطاها با قابلیت چندزبانه‌سازی و لاگ‌گیری.
 */
object ErrorHandler {
    fun getErrorMessage(exception: Throwable, context: Context): String {
        val baseMessage = when (exception) {
            is NetworkException -> context.getString(exception.errorCode.resourceId)
            is IOException -> context.getString(R.string.error_no_internet)
            is TimeoutCancellationException -> context.getString(R.string.error_timeout)
            else -> context.getString(R.string.error_unknown)
        }

        val retryMessage = if (exception is NetworkException && exception.retryCount > 0) {
            "\n" + context.resources.getQuantityString(
                R.plurals.retry_remaining,
                exception.retryCount,
                exception.retryCount
            )
        } else {
            ""
        }

        return baseMessage + retryMessage
    }
}