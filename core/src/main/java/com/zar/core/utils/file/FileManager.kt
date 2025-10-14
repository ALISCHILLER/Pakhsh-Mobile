package com.zar.core.utils.file


import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * کلاس اصلی برای مدیریت فایل‌ها.
 */
class FileManager(private val context: Context) {

    /**
     * ساخت یا بازگرداندن یک پوشه.
     */
    fun getOrCreateDirectory(directoryName: String): File {
        val directory = File(context.filesDir, directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }


    /**
     * فهرست کردن فایل‌های موجود در یک مسیر.
     */
    fun listFiles(directoryName: String, filter: ((File) -> Boolean)? = null): List<File> {
        val directory = getOrCreateDirectory(directoryName)
        val files = directory.listFiles()?.toList().orEmpty()
        return filter?.let { predicate -> files.filter(predicate) } ?: files
    }

    /**
     * خالی کردن یک پوشه. در صورت نیاز خود پوشه نیز حذف می‌شود.
     */
    fun clearDirectory(directoryName: String, removeDirectory: Boolean = false): Boolean {
        val directory = File(context.filesDir, directoryName)
        if (!directory.exists()) return true
        val cleared = directory.listFiles()?.all { it.deleteRecursively() } ?: true
        return if (removeDirectory) {
            cleared && directory.delete()
        } else {
            cleared
        }
    }


    /**
     * ساخت یا بازگرداندن یک فایل.
     */
    fun getOrCreateFile(directoryName: String, fileName: String): File {
        val directory = getOrCreateDirectory(directoryName)
        return File(directory, fileName).apply {
            if (!exists()) {
                parentFile?.mkdirs()
                createNewFile()
            }
        }
    }

    /**
     * ساخت فایل موقت درون پوشه‌ی تعیین شده.
     */
    @Throws(IOException::class)
    fun createTempFile(prefix: String, suffix: String = "", directoryName: String? = null): File {
        val directory = directoryName?.let { getOrCreateDirectory(it) } ?: context.cacheDir
        return File.createTempFile(prefix, suffix, directory)
    }

    /**
     * نوشتن متن به فایل.
     */
    @Throws(IOException::class)
    fun writeToFile(file: File, content: String, append: Boolean = false) {
        file.writer(StandardCharsets.UTF_8, append).use { writer ->
            if (append) {
                writer.appendLine(content)
            } else {
                writer.write(content)
            }
        }
    }

    /**
     * افزودن چند خط به فایل.
     */
    @Throws(IOException::class)
    fun appendLines(file: File, lines: Iterable<String>) {
        file.writer(StandardCharsets.UTF_8, true).use { writer ->
            lines.forEach { line -> writer.appendLine(line) }
        }
    }

    /**
     * نوشتن داده‌ی باینری به فایل.
     */
    @Throws(IOException::class)
    fun writeBytes(file: File, bytes: ByteArray, append: Boolean = false) {
        FileOutputStream(file, append).use { output ->
            output.write(bytes)
        }
    }



    /**
     * خواندن محتویات فایل.
     */
    @Throws(IOException::class)
    fun readFile(file: File): String {
        return file.readText(StandardCharsets.UTF_8)
    }


    /**
     * خواندن خطوط فایل.
     */
    @Throws(IOException::class)
    fun readLines(file: File): List<String> {
        return file.readLines(StandardCharsets.UTF_8)
    }

    /**
     * خواندن داده‌ی باینری از فایل.
     */
    @Throws(IOException::class)
    fun readBytes(file: File): ByteArray {
        return file.readBytes()
    }



    /**
     * بررسی وجود فایل.
     */
    fun fileExists(directoryName: String, fileName: String): Boolean {
        val file = File(getOrCreateDirectory(directoryName), fileName)
        return file.exists()
    }


    /**
     * اندازه‌ی فایل بر حسب بایت.
     */
    fun fileSize(file: File): Long = file.length()

    /**
     * به‌روزرسانی زمان آخرین تغییر فایل یا ساخت آن در صورت عدم وجود.
     */
    fun touch(file: File): Boolean {
        return if (file.exists()) {
            file.setLastModified(System.currentTimeMillis())
        } else {
            file.parentFile?.mkdirs()
            runCatching { file.createNewFile() }
                .onFailure { Timber.e(it, "Failed to create file %s", file.absolutePath) }
                .getOrDefault(false)
        }
    }

    /**
     * کپی کردن فایل به مسیر جدید.
     */
    fun copyFile(source: File, destination: File, overwrite: Boolean = true): Boolean {
        if (!source.exists()) return false
        if (destination.exists() && !overwrite) return false
        destination.parentFile?.mkdirs()
        return runCatching {
            FileInputStream(source).use { input ->
                FileOutputStream(destination, false).use { output ->
                    input.copyTo(output)
                }
            }
            true
        }.onFailure {
            Timber.e(it, "Failed to copy %s to %s", source.absolutePath, destination.absolutePath)
            destination.delete()
        }.getOrDefault(false)
    }

    /**
     * جابجایی فایل به مسیر جدید.
     */
    fun moveFile(source: File, destination: File, overwrite: Boolean = true): Boolean {
        val copied = copyFile(source, destination, overwrite)
        return if (copied) {
            if (source.delete()) {
                true
            } else {
                Timber.w("Failed to delete source file after copy: %s", source.absolutePath)
                false
            }
        } else {
            false
        }
    }

    /**
     * حذف فایل.
     */
    fun deleteFile(directoryName: String, fileName: String): Boolean {
        val file = File(getOrCreateDirectory(directoryName), fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * حذف پوشه به‌همراه تمام فایل‌های آن.
     */
    fun deleteDirectory(directoryName: String): Boolean {
        val directory = File(context.filesDir, directoryName)
        return if (directory.exists()) {
            directory.deleteRecursively()
        } else {
            false
        }
    }
}