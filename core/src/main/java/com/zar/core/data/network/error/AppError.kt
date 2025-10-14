package com.zar.core.data.network.error


sealed class AppError(open val errorCode: String, open val message: String)


data class ConnectionError(
    override val errorCode: String,
    override val message: String,
    val connectionType: String? = null
) : AppError(errorCode, message)


data class TimeoutError(
    override val errorCode: String,
    override val message: String,
    val duration: Long? = null,
    val cause: Throwable? = null
) : AppError(errorCode, message)


data class ParsingError(
    override val errorCode: String,
    override val message: String
) : AppError(errorCode, message)


data class ApiError(
    override val errorCode: String,
    override val message: String,
    val statusCode: Int? = null
) : AppError(errorCode, message)


data class UnknownError(
    override val errorCode: String = "unknown_error",
    override val message: String
) : AppError(errorCode, message)

