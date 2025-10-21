package com.msa.core.common.error

/**
 * Represents a domain friendly error that can be surfaced across layers without
 * leaking transport specific exceptions.
 */
sealed class AppError(
    open val message: String?,
    open val errorCode: String? = null
) {
    data class Network(
        override val message: String?,
        override val errorCode: String? = null,
        val statusCode: Int? = null,
        val isConnectivity: Boolean = false,
        val connectionType: String? = null,
        val cause: Throwable? = null,
        val endpoint: String? = null
    ) : AppError(message, errorCode)

    data class Timeout(
        override val message: String?,
        override val errorCode: String? = null,
        val durationMillis: Long? = null,
        val cause: Throwable? = null,
        val endpoint: String? = null
    ) : AppError(message, errorCode)

    data class Server(
        override val message: String?,
        val statusCode: Int,
        override val errorCode: String? = statusCode.toString(),
        val body: String? = null,
        val endpoint: String = "",
        val requestId: String? = null,
        val headers: Map<String, String> = emptyMap()
    ) : AppError(message, errorCode)

    data class RateLimited(
        override val message: String?,
        val statusCode: Int,
        val retryAfterSeconds: Long? = null,
        val retryAfterRaw: String? = null,
        val endpoint: String? = null,
        val headers: Map<String, String> = emptyMap(),
        override val errorCode: String? = statusCode.toString()
    ) : AppError(message, errorCode)

    data class Auth(
        override val message: String?,
        val reason: String? = null,
        val endpoint: String? = null,
        override val errorCode: String? = reason
    ) : AppError(message, errorCode)

    data class Parsing(
        override val message: String?,
        val raw: String? = null,
        val endpoint: String? = null,
        override val errorCode: String? = null
    ) : AppError(message, errorCode)

    data class Business(
        override val message: String?,
        val payload: Any? = null,
        val businessCode: String? = null,
        override val errorCode: String? = businessCode
    ) : AppError(message, errorCode)

    data class Unknown(
        override val message: String?,
        override val errorCode: String? = null,
        val cause: Throwable? = null,
        val endpoint: String? = null
    ) : AppError(message, errorCode)
}