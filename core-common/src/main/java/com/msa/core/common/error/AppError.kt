package com.msa.core.common.error

/**
 * Represents a domain friendly error that can be surfaced across layers without
 * leaking transport specific exceptions.
 */
sealed class AppError(open val message: String?, open val code: Int? = null) {
    data class Network(
        override val message: String?,
        override val code: Int?,
        val cause: Throwable? = null
    ) : AppError(message, code)

    data class Server(
        override val message: String?,
        override val code: Int,
        val body: String? = null,
        val endpoint: String
    ) : AppError(message, code)

    data class Auth(
        override val message: String?,
        val reason: String? = null
    ) : AppError(message)

    data class Parsing(
        override val message: String?,
        val raw: String?
    ) : AppError(message)

    data class Business(
        override val message: String?,
        val businessCode: String? = null
    ) : AppError(message)

    data class Unknown(
        override val message: String?,
        val cause: Throwable?
    ) : AppError(message)
}