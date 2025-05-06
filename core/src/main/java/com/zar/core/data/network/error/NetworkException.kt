package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.utils.NetworkStatusMonitor
import java.io.IOException

/**
 * Custom exception to represent various network-related issues with meaningful context.
 */
class NetworkException(
    val errorCode: NetworkErrorCode,
    context: Context,
    override val cause: Throwable? = null,
    val retryCount: Int = 0,
    val connectionType: NetworkStatusMonitor.ConnectionType? = null
) : IOException(context.getString(errorCode.resourceId), cause) {

    /**
     * Determines whether this network exception can be retried.
     */
    fun isRetryable(): Boolean {
        return errorCode.shouldRetry && !(
                connectionType == NetworkStatusMonitor.ConnectionType.CELLULAR &&
                        errorCode == NetworkErrorCode.LARGE_DOWNLOAD
                )
    }

    /**
     * Enum representing specific network error codes with user-friendly messages and retry policy.
     */
    enum class NetworkErrorCode(
        val code: Int,
        val resourceId: Int,
        val shouldRetry: Boolean = false
    ) {
        NETWORK_UNAVAILABLE(0, R.string.error_no_connection, true),
        BAD_REQUEST(400, R.string.error_bad_request),
        UNAUTHORIZED(401, R.string.error_unauthorized),
        FORBIDDEN(403, R.string.error_forbidden),
        NOT_FOUND(404, R.string.error_not_found),
        CONFLICT(409, R.string.error_conflict),
        UNPROCESSABLE(422, R.string.error_unprocessable),
        TIMEOUT(408, R.string.error_timeout, true),
        LARGE_DOWNLOAD(413, R.string.error_large_download, false),
        SERVER_ERROR(500, R.string.error_server),
        SERVER_GENERIC(502, R.string.error_server_generic),
        UNKNOWN(-1, R.string.error_unknown);

        companion object {
            fun fromCode(code: Int): NetworkErrorCode {
                return values().find { it.code == code } ?: UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Factory method to create a [NetworkException] from an HTTP status code.
         */
        fun fromStatusCode(
            statusCode: Int,
            context: Context,
            cause: Throwable? = null,
            retryCount: Int = 0,
            connectionType: NetworkStatusMonitor.ConnectionType? = null
        ): NetworkException {
            val code = NetworkErrorCode.fromCode(statusCode)
            return NetworkException(code, context, cause, retryCount, connectionType)
        }
    }
}
