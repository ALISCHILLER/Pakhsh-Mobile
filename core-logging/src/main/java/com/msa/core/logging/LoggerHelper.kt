package com.msa.core.logging

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class LoggerHelper(
    private val logDirectory: File,
    private val json: Json = Json { encodeDefaults = false },
    private val maxFileSizeBytes: Long = 512L * 1024L,
    private val maxHistoryFiles: Int = 5
) {
    private val formatter = DateTimeFormatter.ISO_INSTANT

    fun log(
        level: String,
        tag: String?,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        val sanitizedMessage = maskSecrets(message)
        val sanitizedStack = throwable?.stackTraceToString()?.let(::maskSecrets)
        val combinedMessage = sanitizedStack?.let { stack ->
            if (stack.isBlank()) sanitizedMessage else "$sanitizedMessage\n$stack"
        } ?: sanitizedMessage

        val sanitizedMetadata = metadata.mapValues { (_, value) ->
            when (value) {
                is String -> maskSecrets(value)
                else -> value
            }
        }

        val logLine = LogLine(
            ts = formatter.format(Instant.now().atOffset(ZoneOffset.UTC)),
            level = level,
            tag = tag,
            thread = Thread.currentThread().name,
            msg = combinedMessage,
            userId = sanitizedMetadata["userId"] as? String,
            traceId = sanitizedMetadata["traceId"] as? String,
            endpoint = sanitizedMetadata["endpoint"] as? String,
            status = when (val status = sanitizedMetadata["status"]) {
                is Number -> status.toInt()
                is String -> status.toIntOrNull()
                else -> null
            },
            requestId = sanitizedMetadata["requestId"] as? String
        )

        writeToFile(json.encodeToString(logLine))

        when (level) {
            "ERROR" -> Timber.e(throwable, sanitizedMessage)
            "WARN" -> Timber.w(throwable, sanitizedMessage)
            "INFO" -> Timber.i(sanitizedMessage)
            "DEBUG" -> Timber.d(sanitizedMessage)
            else -> Timber.v(sanitizedMessage)
        }
    }

    private fun writeToFile(line: String) {
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }
        val file = File(logDirectory, CURRENT_FILE_NAME)
        if (file.exists() && file.length() + line.length > maxFileSizeBytes) {
            rotate(file)
        }
        file.appendText("$line\n", StandardCharsets.UTF_8)
    }

    private fun rotate(current: File) {
        val timestamp = formatter.format(Instant.now().atOffset(ZoneOffset.UTC)).replace(":", "-")
        val rotated = File(logDirectory, "logs_$timestamp.json")
        if (current.exists()) {
            current.renameTo(rotated)
        }
        trimHistory()
    }

    private fun trimHistory() {
        if (maxHistoryFiles <= 0) return
        val rotatedFiles = logDirectory.listFiles { _, name ->
            name.startsWith("logs_") && name.endsWith(".json")
        }?.sortedByDescending(File::lastModified).orEmpty()
        rotatedFiles.drop(maxHistoryFiles).forEach { it.delete() }
    }

    private companion object {
        const val CURRENT_FILE_NAME = "logs.json"
    }
}