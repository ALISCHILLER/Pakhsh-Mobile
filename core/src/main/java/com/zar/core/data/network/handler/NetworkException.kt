package com.zar.core.data.network.common

import java.io.IOException

class NetworkException(
    val errorCode: NetworkErrorCode,
    override val message: String,
    val connectionType: String? = null,
    override val cause: Throwable? = null,
    val retryCount: Int = 0
) : IOException(message, cause) {


    fun isRetryable(): Boolean = when (errorCode) {
        NetworkErrorCode.NETWORK_UNAVAILABLE,
        NetworkErrorCode.TIMEOUT,
        NetworkErrorCode.SERVER_ERROR,
        NetworkErrorCode.SERVER_GENERIC -> true
        else -> false
    }


    enum class NetworkErrorCode(val code: Int) {
        NETWORK_UNAVAILABLE(0),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        CONFLICT(409),
        UNPROCESSABLE(422),
        TIMEOUT(408),
        LARGE_DOWNLOAD(413),
        SERVER_ERROR(500),
        SERVER_GENERIC(502),
        UNKNOWN(-1);


        companion object {
            fun fromHttpCode(code: Int): NetworkErrorCode = when (code) {
                400 -> BAD_REQUEST
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN
                404 -> NOT_FOUND
                409 -> CONFLICT
                422 -> UNPROCESSABLE
                413 -> LARGE_DOWNLOAD
                in 500..599 -> SERVER_ERROR
                408 -> TIMEOUT
                502 -> SERVER_GENERIC
                else -> UNKNOWN
            }
        }
    }
}