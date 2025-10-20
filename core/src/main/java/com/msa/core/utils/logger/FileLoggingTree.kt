package com.msa.core.utils.logger

import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * درخت Timber برای نوشتن WARN/ERROR در فایل، با rotation ساده و ایمن در برابر recursion.
 */
class FileLoggingTree(
    internal val file: File,
    private val maxBytes: Long = 512 * 1024,   // 512KB
    private val maxBackups: Int = 3,           // تعداد backup ها: file.1, file.2, ...
    private val maxFileAgeMillis: Long? = null // دوران حداکثر عمر فایل برای rotation زمان‌محور
) : Timber.Tree() {

    private val lock = Any()
    private val tsFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // فقط WARN/ERROR
        if (priority != Log.WARN && priority != Log.ERROR) return

        val logMessage = buildLogLine(priority, tag, message, t)

        synchronized(lock) {
            try {
                ensureParent()
                rotateIfNeeded()
                FileWriter(file, /*append=*/true).use { w ->
                    w.append(logMessage).append('\n')
                    w.flush()
                }
            } catch (e: IOException) {
                // خیلی مهم: اینجا از Timber.* استفاده نکن تا recursion ایجاد نشود
                Log.e("FileLoggingTree", "Error writing log file: ${e.message}", e)
            }
        }
    }

    private fun ensureParent() {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) parent.mkdirs()
        if (!file.exists()) runCatching { file.createNewFile() }.getOrNull()
    }

    private fun rotateIfNeeded() {
        if (!file.exists()) return
        if (!shouldRotateBySize() && !shouldRotateByAge()) return

        // delete oldest
        val oldest = File(file.parent, "${file.name}.$maxBackups")
        if (oldest.exists()) oldest.delete()

        // shift .(n-1) -> .n
        for (i in maxBackups - 1 downTo 1) {
            val src = File(file.parent, "${file.name}.$i")
            if (src.exists()) {
                val dst = File(file.parent, "${file.name}.${i + 1}")
                src.renameTo(dst)
            }
        }
        // current -> .1
        val first = File(file.parent, "${file.name}.1")
        file.renameTo(first)
        // create fresh file
        runCatching { file.createNewFile() }.getOrNull()
    }

    private fun shouldRotateBySize(): Boolean = file.length() >= maxBytes

    private fun shouldRotateByAge(): Boolean {
        val maxAge = maxFileAgeMillis ?: return false
        val lastModified = file.lastModified()
        if (lastModified <= 0L) return false
        return System.currentTimeMillis() - lastModified >= maxAge
    }


    private fun buildLogLine(priority: Int, tag: String?, message: String, t: Throwable?): String {
        val ts = tsFormat.format(Date())
        val pr = when (priority) {
            Log.VERBOSE -> "VERBOSE"
            Log.DEBUG   -> "DEBUG"
            Log.INFO    -> "INFO"
            Log.WARN    -> "WARN"
            Log.ERROR   -> "ERROR"
            Log.ASSERT  -> "ASSERT"
            else        -> "UNKNOWN"
        }
        val threadName = Thread.currentThread().name
        val stack = t?.let { "\n${Log.getStackTraceString(it)}" } ?: ""
        val tg = tag ?: "TAG"
        return "$ts | $pr | $tg | [$threadName] | $message$stack"
    }
}
