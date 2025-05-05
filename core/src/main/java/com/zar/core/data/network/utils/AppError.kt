package com.zar.core.data.network.utils

/**
 * پایه‌ترین نوع خطاهای سیستم
 */
sealed interface AppError {
    val cause: Throwable?
    val message: String
    val metadata: Map<String, Any>
}

/**
 * خطاهای مربوط به شبکه
 */
sealed class NetworkError : AppError {
    data class ConnectionError(
        override val cause: Throwable?,
        override val message: String,
        val connectionType: ConnectionType?,
        override val metadata: Map<String, Any> = emptyMap()
    ) : NetworkError()

    data class HttpError(
        val statusCode: Int,
        override val cause: Throwable?,
        override val message: String,
        val responseBody: String?,
        override val metadata: Map<String, Any> = emptyMap()
    ) : NetworkError()

    data class TimeoutError(
        override val cause: Throwable?,
        override val message: String,
        val duration: Long,
        override val metadata: Map<String, Any> = emptyMap()
    ) : NetworkError()
}

/**
 * خطاهای مربوط به سرور و API
 */
sealed class ServerError : AppError {
    data class ApiError(
        val errorCode: String,
        override val cause: Throwable?,
        override val message: String,
        val details: Map<String, String>?,
        override val metadata: Map<String, Any> = emptyMap()
    ) : ServerError()

    data class ParsingError(
        override val cause: Throwable?,
        override val message: String,
        val rawResponse: String?,
        override val metadata: Map<String, Any> = emptyMap()
    ) : ServerError()
}

/**
 * خطاهای مربوط به دامنه و منطق کسب‌وکار
 */
sealed class DomainError : AppError {
    data class ValidationError(
        override val cause: Throwable?,
        override val message: String,
        val fieldErrors: Map<String, String>,
        override val metadata: Map<String, Any> = emptyMap()
    ) : DomainError()

    data class BusinessRuleError(
        val ruleCode: String,
        override val cause: Throwable?,
        override val message: String,
        override val metadata: Map<String, Any> = emptyMap()
    ) : DomainError()
}