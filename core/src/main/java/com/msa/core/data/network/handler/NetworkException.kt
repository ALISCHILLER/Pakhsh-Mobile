package com.msa.core.data.network.handler

import android.content.Context
import com.msa.core.R
import com.msa.core.data.network.model.ApiResponse
import timber.log.Timber
import java.io.IOException

/**
 * استثناهای سفارشی برای مدیریت خطاهای شبکه با پشتیبانی از:
 * - کدهای HTTP استاندارد
 * - پیام‌های چندزبانه
 * - تعداد تلاش‌های مجدد
 * - قابلیت تشخیص خطاها با استفاده از Enum
 */
class NetworkException(
    val errorCode: NetworkErrorCode,
    context: Context,
    override val cause: Throwable? = null,
    val retryCount: Int = 0
) : IOException(context.getString(errorCode.resourceId), cause) {

    fun isRetryable(): Boolean = errorCode.shouldRetry // اضافه شود

    val httpStatus: Int get() = errorCode.code

    override fun toString(): String {
        return "NetworkException(code=${errorCode.code}, name=${errorCode.name}, retryCount=$retryCount)"
    }

    companion object {
        fun fromStatusCode(
            statusCode: Int,
            context: Context,
            cause: Throwable? = null,
            retryCount: Int = 0
        ): NetworkException {
            val code = NetworkErrorCode.fromCode(statusCode)
            return NetworkException(code, context, cause, retryCount)
        }

        fun fromApiResponse(
            apiResponse: ApiResponse<*>,
            context: Context
        ): NetworkException {
            val code = NetworkErrorCode.fromCode(apiResponse.code ?: -1)
            return NetworkException(code, context, message = apiResponse.message)
        }
    }

    enum class NetworkErrorCode(
        val code: Int,
        val resourceId: Int,
        val shouldRetry: Boolean = false
    ) {
        NETWORK_UNAVAILABLE(503, R.string.error_no_internet, true),
        BAD_REQUEST(400, R.string.error_bad_request),
        UNAUTHORIZED(401, R.string.error_unauthorized),
        FORBIDDEN(403, R.string.error_forbidden),
        NOT_FOUND(404, R.string.error_not_found),
        CONFLICT(409, R.string.error_conflict),
        UNPROCESSABLE(422, R.string.error_unprocessable),
        TIMEOUT(408, R.string.error_timeout, true),
        SERVER_ERROR(500, R.string.error_server, true),
        UNKNOWN(-1, R.string.error_unknown);


        companion object {
            fun fromCode(code: Int): NetworkErrorCode =
                values().find { it.code == code } ?: UNKNOWN
        }
    }
}