package com.zar.core.utils.logger

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

object LoggerHelper {

    private val initialized = AtomicBoolean(false)
    private var logFile: File? = null

    /**
     * راه‌اندازی Timber و FileLoggingTree.
     * اگر tree پاس ندهید، در مسیر /files/logs/error_log.txt ساخته می‌شود.
     */
    @JvmStatic
    @Synchronized
    fun init(
        context: Context,
        tree: FileLoggingTree? = null,
        logFileName: String = "error_log.txt"
    ) {
        if (initialized.get()) return

        val logDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
        val activeTree = tree ?: FileLoggingTree(File(logDir, logFileName))

        val alreadyPlanted = Timber.forest().any {
            it is FileLoggingTree && it.file.absolutePath == activeTree.file.absolutePath
        }
        if (!alreadyPlanted) Timber.plant(activeTree)

        logFile = activeTree.file
        initialized.set(true)
    }

    /** وضعیت اولیه‌سازی LoggerHelper. */
    fun isInitialized(): Boolean = initialized.get()

    /** دسترسی به فایل فعلی لاگ (ممکن است وجود نداشته باشد). */
    fun currentLogFile(): File? = logFile?.takeIf { it.exists() }

    /**
     * خواندن لاگ.
     * @param maxBytes اگر فایل خیلی بزرگ است، فقط tail را برمی‌گرداند.
     */
    fun readLogFile(maxBytes: Long = 256_000): String? {
        val file = currentLogFile() ?: return null
        return runCatching {
            val len = file.length()
            if (len <= maxBytes) {
                file.readText(StandardCharsets.UTF_8)
            } else {
                file.inputStream().use { input ->
                    val skip = max(0L, len - maxBytes)
                    if (skip > 0) input.skip(skip)
                    String(input.readBytes(), StandardCharsets.UTF_8)
                }
            }
        }.onFailure {
            Log.e("LoggerHelper", "Error reading log file: ${it.message}", it)
        }.getOrNull()
    }

    /** پاک‌کردن فایل لاگ. */
    fun clearLogFile(): Boolean {
        val file = currentLogFile() ?: return false
        return runCatching { file.delete() }
            .onFailure { Log.e("LoggerHelper", "Error deleting log file: ${it.message}", it) }
            .getOrDefault(false)
    }
}
