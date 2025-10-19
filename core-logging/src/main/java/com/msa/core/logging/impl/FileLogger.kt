package com.msa.core.logging.impl

import com.msa.core.logging.api.LogContext
import com.msa.core.logging.api.LogEntry
import com.msa.core.logging.api.LogLevel
import com.msa.core.logging.api.LogExporter
import com.msa.core.logging.api.Logger
import java.io.File
import java.nio.charset.Charset
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.withLock
import java.util.concurrent.locks.ReentrantLock

/**
 * Lightweight JSON-line logger implementation that writes to rotating files.
 */
class FileLogger(
    private val logDir: File,
    private val maxFileSizeBytes: Long = DEFAULT_MAX_FILE_SIZE,
    private val maxFiles: Int = DEFAULT_MAX_FILES,
    private val charset: Charset = Charsets.UTF_8,
    private val clock: () -> Instant = { Instant.now() }
) : Logger, LogExporter {

    private val lock = ReentrantLock()
    private val baseName = "msa-log"

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
    }

    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?, context: LogContext): Unit = lock.withLock {
        val entry = serialize(
            LogEntry(
                timestamp = clock(),
                level = level,
                tag = tag,
                message = maskSensitive(message),
                throwable = throwable?.stackTraceToString()?.let(::maskSensitive),
                context = maskContext(context)
            )
        )
        writeEntry(entry)
    }

    override fun withContext(context: LogContext): Logger = ContextualLogger(this, context)

    override fun export(destination: File): File = lock.withLock {
        val zipFile = if (destination.isDirectory) {
            destination.resolve("logs-${DateTimeFormatter.ISO_INSTANT.format(clock())}.zip")
        } else destination

        ZipOutputStream(zipFile.outputStream()).use { zip ->
            activeLogFiles().forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { input ->
                    input.copyTo(zip)
                }
                zip.closeEntry()
            }
        }
        zipFile
    }

    private fun writeEntry(entry: String) {
        val current = currentFile()
        current.appendText(entry + "\n", charset)
        rotateIfNeeded(current)
    }

    private fun rotateIfNeeded(file: File) {
        if (file.length() <= maxFileSizeBytes) return
        val files = activeLogFiles().sortedBy(File::lastModified).toMutableList()
        while (files.size >= maxFiles) {
            files.removeFirst().delete()
        }
        val rotatedName = "${file.nameWithoutExtension}-${System.currentTimeMillis()}.log"
        val rotated = logDir.resolve(rotatedName)
        file.copyTo(rotated, overwrite = true)
        file.writeText("")
    }

    private fun currentFile(): File = logDir.resolve("$baseName.log")

    private fun activeLogFiles(): List<File> = logDir.listFiles { f -> f.extension == "log" }?.toList() ?: emptyList()

    private fun serialize(entry: LogEntry): String = buildString {
        append('{')
        appendJsonField("ts", DateTimeFormatter.ISO_INSTANT.format(entry.timestamp)); append(',')
        appendJsonField("level", entry.level.name); append(',')
        appendJsonField("tag", entry.tag); append(',')
        appendJsonField("message", entry.message); append(',')
        appendJsonField("throwable", entry.throwable); append(',')
        append("\"context\":{")
        appendJsonField("userId", entry.context.userId); append(',')
        appendJsonField("traceId", entry.context.traceId); append(',')
        appendJsonField("requestId", entry.context.requestId); append(',')
        append("\"extras\":{")
        entry.context.extras.entries.joinToString(separator = ",") {
            "\"${escape(it.key)}\":\"${escape(it.value)}\""
        }.let(::append)
        append('}'); append('}'); append('}')
    }

    private fun StringBuilder.appendJsonField(name: String, value: String?) {
        append('"').append(escape(name)).append('"').append(':')
        if (value == null) {
            append("null")
        } else {
            append('"').append(escape(value)).append('"')
        }
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }

    private fun maskSensitive(value: String): String = value
        .replace(Regex("(?i)authorization:?[ ]*([^\\s]+)"), "Authorization: ***")
        .replace(Regex("(?i)token:?[ ]*([^\\s]+)"), "Token: ***")

    private fun maskContext(context: LogContext): LogContext = context.copy(
        extras = context.extras.mapValues { (_, v) -> maskSensitive(v) }
    )

    private class ContextualLogger(
        private val delegate: FileLogger,
        private val context: LogContext
    ) : Logger {
        override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?, context: LogContext) {
            delegate.log(level, tag, message, throwable, delegate.maskContext(this.context.merge(context)))
        }

        override fun withContext(context: LogContext): Logger = ContextualLogger(delegate, this.context.merge(context))
    }

    companion object {
        private const val DEFAULT_MAX_FILE_SIZE = 512 * 1024L // 512 KB
        private const val DEFAULT_MAX_FILES = 5
    }
}