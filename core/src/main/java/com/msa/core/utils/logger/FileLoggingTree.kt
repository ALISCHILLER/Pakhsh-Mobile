package com.msa.core.utils.logger


import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException

class FileLoggingTree(private val logFile: File) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // ذخیره فقط خطاها و هشدارها
        if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()  // اگر فایل وجود ندارد، آن را بساز
                } catch (e: IOException) {
                    Timber.e(e, "Error creating log file")
                }
            }

            val logMessage = buildLogMessage(priority, tag, message, t)

            try {
                val writer = FileWriter(logFile, true)
                writer.append(logMessage)
                writer.append("\n")
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                Timber.e(e, "Error writing log to file")
            }
        }
    }

    private fun buildLogMessage(priority: Int, tag: String?, message: String, t: Throwable?): String {
        val priorityString = when (priority) {
            android.util.Log.VERBOSE -> "VERBOSE"
            android.util.Log.DEBUG -> "DEBUG"
            android.util.Log.INFO -> "INFO"
            android.util.Log.WARN -> "WARN"
            android.util.Log.ERROR -> "ERROR"
            android.util.Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
        val throwableMessage = t?.let { "\n${android.util.Log.getStackTraceString(it)}" } ?: ""
        return "${priorityString} | ${tag ?: "TAG"} | $message$throwableMessage"
    }
}
