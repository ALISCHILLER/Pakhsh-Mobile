package com.zar.core.utils.logger

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

object LoggerHelper {

    private lateinit var logFile: File
    private var initialized = false
    /**
     * تنظیمات اولیه برای Timber
     */
    fun init(
        context: Context,
        tree: FileLoggingTree? = null,
        logFileName: String = "error_log.txt"
    ) {
        if (initialized) return

        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        val activeTree = tree ?: FileLoggingTree(File(logDir, logFileName))
        plantIfMissing(activeTree)
        logFile = activeTree.file

        initialized = true
    }

    private fun plantIfMissing(tree: FileLoggingTree) {
        val forest = Timber.forest()
        if (!forest.contains(tree)) {
            Timber.plant(tree)
        }
    }

    // برای لاگ کردن پیام‌های Debug
    fun d(message: String, throwable: Throwable? = null) {
        Timber.d(throwable, message)
    }

    // برای لاگ کردن پیام‌های Info
    fun i(message: String, throwable: Throwable? = null) {
        Timber.i(throwable, message)
    }

    // برای لاگ کردن پیام‌های Warning
    fun w(message: String, throwable: Throwable? = null) {
        Timber.w(throwable, message)
    }

    // برای لاگ کردن پیام‌های Error
    fun e(message: String, throwable: Throwable? = null) {
        Timber.e(throwable, message)
    }

    // برای لاگ کردن پیام‌های Verbose
    fun v(message: String, throwable: Throwable? = null) {
        Timber.v(throwable, message)
    }

    // برای لاگ کردن پیام‌های Assert
    fun wtf(message: String, throwable: Throwable? = null) {
        Timber.wtf(throwable, message)
    }

    // خواندن محتویات فایل لاگ
    fun readLogFile(): String? {
        return if (::logFile.isInitialized && logFile.exists()) {
            logFile.readText(StandardCharsets.UTF_8)
        } else {
            null
        }
    }

    // پاک کردن فایل لاگ
    fun clearLogFile() {
        if (::logFile.isInitialized && logFile.exists()) {
            try {
                logFile.delete()
            } catch (e: IOException) {
                Timber.e(e, "Error deleting log file")
            }
        }
    }
}





//val loggerHelper: LoggerHelper by inject()
//var logContent by remember { mutableStateOf("") }
//
//// ایجاد لاگ جدید
//loggerHelper.e("این یک پیام خطا است!")
//loggerHelper.w("این یک پیام هشدار است!")
//
//// خواندن محتویات فایل لاگ
//logContent = LoggerHelper.readLogFile() ?: "فایل لاگ خالی است."
//// پاک کردن فایل لاگ
//LoggerHelper.clearLogFile()
//logContent = "فایل لاگ پاک شد!"


//private val loggerHelper: LoggerHelper by inject()
//
//fun logSomething() {
//    loggerHelper.e("این یک پیام خطا است!")
//    loggerHelper.w("این یک پیام هشدار است!")
//}
//
//val logContent = LoggerHelper.readLogFile()
//println(logContent)
//LoggerHelper.clearLogFile()