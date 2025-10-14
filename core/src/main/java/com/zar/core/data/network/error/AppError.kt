package com.zar.core.data.network.error

/**
 * Typed representation of all recoverable errors surfaced by the networking layer.
 */
sealed class AppError(open val message: String) {
    data class Http(override val message: String, val code: Int) : AppError(message)
    data class Network(override val message: String, val isTimeout: Boolean = false) : AppError(message)
    data class Serialization(override val message: String) : AppError(message)
    data class Unauthorized(override val message: String = "Unauthorized") : AppError(message)
    data class Forbidden(override val message: String = "Forbidden") : AppError(message)
    data class NotFound(override val message: String = "Not Found") : AppError(message)
    data class Server(override val message: String = "Server error") : AppError(message)
    data class Canceled(override val message: String = "Canceled") : AppError(message)
    data class Unknown(override val message: String = "Unknown error") : AppError(message)
}

typealias UnknownError = AppError.Unknown
