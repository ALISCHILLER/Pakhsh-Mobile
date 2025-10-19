package com.msa.core.logging.api

import java.io.File
import java.time.Instant

/**
 * Simple structured logging contract that core modules can depend on.
 */
interface Logger {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null, context: LogContext = LogContext.EMPTY)
    fun withContext(context: LogContext): Logger
}

data class LogContext(
    val userId: String? = null,
    val traceId: String? = null,
    val requestId: String? = null,
    val extras: Map<String, String> = emptyMap()
) {
    fun merge(other: LogContext): LogContext = LogContext(
        userId = other.userId ?: userId,
        traceId = other.traceId ?: traceId,
        requestId = other.requestId ?: requestId,
        extras = extras + other.extras
    )

    companion object {
        val EMPTY = LogContext()
    }
}

enum class LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

/**
 * Exports logs to an encrypted zip bundle so that they can be shared with support.
 */
interface LogExporter {
    fun export(destination: File): File
}

/**
 * Represents a serialized log entry.
 */
data class LogEntry(
    val timestamp: Instant,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: String?,
    val context: LogContext
)