package com.zar.core.data.network.error

import com.zar.core.data.network.utils.NetworkStatusMonitor
import java.io.IOException
import java.lang.Exception

/**
 * پایه تمام خطاهای برنامه - شامل metadata برای لاگ‌گیری و reporting
 */
sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : IOException(message, cause)

// Network Errors
sealed class NetworkError(
    message: String? = null,
    cause: Throwable? = null
) : AppError(message, cause)

data class ConnectionError(
    override val message: String,
    override val cause: Throwable?,
    val connectionType: NetworkStatusMonitor.ConnectionType?,
    val metadata: Map<String, Any> = emptyMap()
) : NetworkError(message, cause)

data class TimeoutError(
    override val message: String,
    override val cause: Throwable?,
    val duration: Long,
    val metadata: Map<String, Any> = emptyMap()
) : NetworkError(message, cause)

// Server Errors
sealed class ServerError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : AppError(message, cause)

data class ApiError(
    val errorCode: String,
    override val message: String,
    override val cause: Throwable?,
    val details: Map<String, String>? = null,
    val metadata: Map<String, Any> = emptyMap()
) : ServerError(message, cause)

data class ParsingError(
    override val message: String,
    override val cause: Throwable?,
    val rawResponse: String?,
    val metadata: Map<String, Any> = emptyMap()
) : ServerError(message, cause)

// Domain / Business Logic Errors
sealed class DomainError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : AppError(message, cause)

data class ValidationError(
    override val message: String,
    override val cause: Throwable?,
    val fieldErrors: Map<String, String>,
    val metadata: Map<String, Any> = emptyMap()
) : DomainError(message, cause)

data class BusinessRuleError(
    val ruleCode: String,
    override val message: String,
    override val cause: Throwable?,
    val metadata: Map<String, Any> = emptyMap()
) : DomainError(message, cause)