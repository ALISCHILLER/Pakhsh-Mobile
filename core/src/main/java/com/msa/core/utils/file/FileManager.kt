package com.msa.core.utils.file

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.math.max

/**
 * مدیریت فایل‌ها داخل فضای app (filesDir / cacheDir).
 */
class FileManager(private val context: Context) {

    private val baseDir: File = context.filesDir

    /** جلوگیری از مسیرهای خارج از فضای برنامه (path traversal). */
    private fun safeResolve(parent: File, vararg segments: String): File {
        val target = segments.fold(parent) { acc, seg -> File(acc, seg) }
        val basePath = parent.canonicalFile
        val realPath = target.canonicalFile
        if (!realPath.path.startsWith(basePath.path)) {
            throw SecurityException("Path escapes app directory: ${realPath.path}")
        }
        return realPath
    }

    // ---------------- Directories ----------------

    /** ساخت یا بازگرداندن یک پوشه. */
    fun getOrCreateDirectory(directoryName: String): File {
        val directory = safeResolve(baseDir, directoryName)
        if (!directory.exists()) directory.mkdirs()
        return directory
    }

    /** فهرست‌کردن فایل‌های یک مسیر (بدون ساخت پوشه). */
    fun listFiles(directoryName: String, filter: ((File) -> Boolean)? = null): List<File> {
        val directory = safeResolve(baseDir, directoryName)
        val files = directory.listFiles()?.toList().orEmpty()
        return filter?.let { predicate -> files.filter(predicate) } ?: files
    }

    /** نسخه‌ای که یک File موجود را می‌پذیرد. */
    fun listFiles(directory: File, filter: ((File) -> Boolean)? = null): List<File> {
        if (!directory.exists() || !directory.isDirectory) return emptyList()
        val files = directory.listFiles()?.toList().orEmpty()
        return filter?.let { predicate -> files.filter(predicate) } ?: files
    }


    /** خالی‌کردن یک پوشه؛ در صورت نیاز خود پوشه هم حذف می‌شود. */
    fun clearDirectory(directoryName: String, removeDirectory: Boolean = false): Boolean {
        val directory = safeResolve(baseDir, directoryName)
        if (!directory.exists()) return true
        val cleared = directory.listFiles()?.all { it.deleteRecursively() } ?: true
        return if (removeDirectory) cleared && directory.delete() else cleared
    }

    /** حذف پوشه به‌همراه تمام فایل‌های آن. */
    fun deleteDirectory(directoryName: String): Boolean {
        val directory = safeResolve(baseDir, directoryName)
        return if (directory.exists()) directory.deleteRecursively() else false
    }

    // ---------------- Files ----------------

    /** ساخت یا بازگرداندن یک فایل. */
    fun getOrCreateFile(directoryName: String, fileName: String): File {
        val directory = getOrCreateDirectory(directoryName)
        val file = safeResolve(directory, fileName)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            runCatching { file.createNewFile() }
                .onFailure { Timber.e(it, "Failed to create file %s", file.absolutePath) }
        }
        return file
    }

    /** بررسی وجود فایل (بدون ساخت پوشه). */
    fun fileExists(directoryName: String, fileName: String): Boolean {
        val directory = safeResolve(baseDir, directoryName)
        val file = safeResolve(directory, fileName)
        return file.exists()
    }

    /** اندازه‌ی فایل بر حسب بایت. */
    fun fileSize(file: File): Long = if (file.exists()) file.length() else 0L

    /** آیا فایل خالی است؟ (وجود + طول) */
    fun isFileEmpty(file: File): Boolean = !file.exists() || file.length() == 0L

    /** بروزرسانی mtime یا ساخت اگر وجود ندارد. */
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

    /** حذف فایل. */
    fun deleteFile(directoryName: String, fileName: String): Boolean {
        val directory = safeResolve(baseDir, directoryName)
        val file = safeResolve(directory, fileName)
        return file.exists() && file.delete()
    }

    /** اطمینان از وجود دایرکتوری مشخص. */
    fun ensureDirectory(directory: File): Boolean =
        (directory.exists() && directory.isDirectory) || directory.mkdirs()

    /** حذف بی‌سروصدا (بدون exception) برای فایل یا پوشه. */
    fun deleteQuietly(target: File): Boolean =
        if (!target.exists()) false
        else runCatching {
            if (target.isDirectory) target.deleteRecursively() else target.delete()
        }.getOrDefault(false)


    // ---------------- Write ----------------

    @Throws(IOException::class)
    fun writeToFile(
        file: File,
        content: String,
        append: Boolean = false,
        charset: Charset = StandardCharsets.UTF_8
    ) {
        file.parentFile?.mkdirs()
        OutputStreamWriter(FileOutputStream(file, append), charset).use { writer ->
            writer.write(content)
            writer.flush()
        }
    }

    @Throws(IOException::class)
    fun appendLines(
        file: File,
        lines: Iterable<String>,
        charset: Charset = StandardCharsets.UTF_8
    ) {
        file.parentFile?.mkdirs()
        OutputStreamWriter(FileOutputStream(file, /* append = */ true), charset).use { writer ->
            lines.forEach { line ->
                writer.write(line)
                writer.write("\n")
            }
            writer.flush()
        }
    }

    @Throws(IOException::class)
    fun writeBytes(file: File, bytes: ByteArray, append: Boolean = false) {
        file.parentFile?.mkdirs()
        FileOutputStream(file, append).use { output ->
            output.write(bytes)
        }
    }

    // ---------------- Read ----------------

    /** خواندن متن (با امکان محدودکردن حداکثر بایت برای جلوگیری از OOM). */
    @Throws(IOException::class)
    fun readFile(
        file: File,
        charset: Charset = StandardCharsets.UTF_8,
        maxBytes: Long = Long.MAX_VALUE
    ): String {
        if (!file.exists()) throw IOException("File not found: ${file.absolutePath}")
        return if (file.length() <= maxBytes) {
            file.readText(charset)
        } else {
            // فقط tail فایل را می‌خوانیم
            FileInputStream(file).use { fis ->
                val skip = max(0L, file.length() - maxBytes)
                if (skip > 0) fis.skip(skip)
                val bytes = fis.readBytes()
                String(bytes, charset)
            }
        }
    }

    @Throws(IOException::class)
    fun readLines(file: File, charset: Charset = StandardCharsets.UTF_8): List<String> {
        if (!file.exists()) throw IOException("File not found: ${file.absolutePath}")
        return file.readLines(charset)
    }

    @Throws(IOException::class)
    fun readBytes(file: File): ByteArray {
        if (!file.exists()) throw IOException("File not found: ${file.absolutePath}")
        return file.readBytes()
    }

    // ---------------- Copy / Move ----------------

    /**
     * کپی فایل با overwrite اختیاری.
     * در خطا، مقصد پاک می‌شود.
     */
    fun copyFile(
        source: File,
        destination: File,
        overwrite: Boolean = true,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Boolean {
        if (!source.exists()) return false
        if (destination.exists() && !overwrite) return false
        destination.parentFile?.mkdirs()

        return runCatching {
            FileInputStream(source).use { input ->
                FileOutputStream(destination, false).use { output ->
                    input.copyTo(output, bufferSize)
                }
            }
            // در صورت نیاز، mtime را هم کپی کنید:
            destination.setLastModified(source.lastModified())
            true
        }.onFailure {
            Timber.e(it, "Failed to copy %s -> %s", source.absolutePath, destination.absolutePath)
            runCatching { destination.delete() }
        }.getOrDefault(false)
    }

    /**
     * جابجایی فایل: ابتدا renameTo (اتمیک اگر روی یک پارتیشن باشد)،
     * اگر نشد، کپی + حذف.
     */
    fun moveFile(
        source: File,
        destination: File,
        overwrite: Boolean = true,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Boolean {
        if (!source.exists()) return false
        if (destination.exists()) {
            if (!overwrite) return false
            // اگر overwrite=true و مقصد هست، پاکش کن تا rename موفق شود
            if (!destination.delete()) {
                Timber.w("Failed to delete existing destination: %s", destination.absolutePath)
            }
        }
        destination.parentFile?.mkdirs()

        // مسیر سریع: rename اتمیک
        if (source.renameTo(destination)) return true

        // fallback: copy + delete
        val copied = copyFile(source, destination, overwrite = true, bufferSize = bufferSize)
        return if (copied) {
            if (source.delete()) {
                true
            } else {
                Timber.w("Failed to delete source after copy: %s", source.absolutePath)
                false
            }
        } else {
            false
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE: Int = 8 * 1024
    }
}
