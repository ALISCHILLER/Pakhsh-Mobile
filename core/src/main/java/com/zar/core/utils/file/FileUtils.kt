package com.zar.core.utils.file

import java.io.File

/**
 * ابزارهای کمکی برای مدیریت فایل‌ها.
 */
object FileUtils {

    /**
     * محاسبه اندازه‌ی فایل (به بایت).
     * اگر فایل وجود نداشته باشد، 0 برمی‌گرداند.
     */
    fun getFileSize(file: File): Long =
        if (file.exists()) file.length() else 0L

    /**
     * بررسی خالی‌بودن فایل.
     * اگر فایل وجود نداشته باشد، true برمی‌گرداند (معادل خالی).
     */
    fun isFileEmpty(file: File): Boolean =
        !file.exists() || file.length() == 0L

    /**
     * لیست کردن تمام فایل‌های موجود در یک پوشه (فقط سطح اول).
     * اگر پوشه وجود نداشته باشد یا پوشه نباشد، لیست خالی برمی‌گرداند.
     */
    fun listFilesInDirectory(directory: File): List<File> =
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.toList().orEmpty()
        } else {
            emptyList()
        }

    // --- ابزارهای مفید تکمیلی ---

    /**
     * اطمینان از وجود پوشه (در صورت نبود، می‌سازد).
     * true اگر پوشه موجود/ساخته شد.
     */
    fun ensureDirectory(directory: File): Boolean =
        directory.exists() && directory.isDirectory || directory.mkdirs()

    /**
     * حذف فایل یا پوشه به‌صورت بی‌سروصدا (بدون خطا).
     * false اگر وجود نداشت یا حذف نشد.
     */
    fun deleteQuietly(target: File): Boolean =
        if (!target.exists()) false else runCatching {
            if (target.isDirectory) target.deleteRecursively() else target.delete()
        }.getOrDefault(false)
}
