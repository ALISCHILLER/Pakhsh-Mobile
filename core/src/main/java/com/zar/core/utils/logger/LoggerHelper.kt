package com.zar.core.utils.logger

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

object LoggerHelper {

    private lateinit var logFile: File
    private val initialized = AtomicBoolean(false)

    /**
     * راه‌اندازی Timber و FileLoggingTree.
     * اگر tree پاس ندهید، در مسیر /files/logs/error_log.txt ساخته می‌شود.
     */
    @JvmStatic
    @Synchronized
    fun init(
        context: Context,
        tree: FileLoggingTree? = null,
        logFileName: String = "error_log.txt",
        maxBytes: Long = 512 * 1024,
        maxBackups: Int = 3
    ) {
        if (initialized.get()) return

        val logDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
        val activeTree = tree ?: FileLoggingTree(File(logDir, logFileName), maxBytes, maxBackups)

        // اگر قبلاً درخت مشابه کاشته شده، دوباره نکاریم
        val alreadyPlanted = Timber.forest().any {
            it is FileLoggingTree && it.file.absolutePath == activeTree.file.absolutePath
        }
        if (!alreadyPlanted) Timber.plant(activeTree)

        logFile = activeTree.file
        initialized.set(true)
    }

    /** دسترسی به فایل فعلی لاگ (ممکن است وجود نداشته باشد). */
    fun currentLogFile(): File? = if (::logFile.isInitialized) logFile else null

    // ---- proxy های Timber (این‌ها recursion ایجاد نمی‌کنند چون خارج از Tree.log هستند) ----
    fun d(message: String, throwable: Throwable? = null) = Timber.d(throwable, message)
    fun i(message: String, throwable: Throwable? = null) = Timber.i(throwable, message)
    fun w(message: String, throwable: Throwable? = null) = Timber.w(throwable, message)
    fun e(message: String, throwable: Throwable? = null) = Timber.e(throwable, message)
    fun v(message: String, throwable: Throwable? = null) = Timber.v(throwable, message)
    fun wtf(message: String, throwable: Throwable? = null) = Timber.wtf(throwable, message)

    /**
     * خواندن لاگ.
     * @param maxBytes اگر فایل خیلی بزرگ است، فقط tail را برمی‌گرداند.
     */
    fun readLogFile(maxBytes: Long = 256_000): String? {
        val f = currentLogFile() ?: return null
        if (!f.exists()) return null
        return runCatching {
            val len = f.length()
            if (len <= maxBytes) {
                f.readText(StandardCharsets.UTF_8)
            } else {
                f.inputStream().use { ins ->
                    val skip = max(0L, len - maxBytes)
                    if (skip > 0) ins.skip(skip)
                    String(ins.readBytes(), StandardCharsets.UTF_8)
                }
            }
        }.onFailure {
            Log.e("LoggerHelper", "Error reading log file: ${it.message}", it)
        }.getOrNull()
    }

    /** پاک‌کردن فایل لاگ. */
    fun clearLogFile(): Boolean {
        val f = currentLogFile() ?: return false
        if (!f.exists()) return false
        return runCatching { f.delete() }
            .onFailure { Log.e("LoggerHelper", "Error deleting log file: ${it.message}", it) }
            .getOrDefault(false)
    }
}
