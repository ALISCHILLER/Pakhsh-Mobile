package com.msa.core.utils.file


import java.io.File

/**
 * ابزارهای کمکی برای مدیریت فایل‌ها.
 */
object FileUtils {

    /**
     * محاسبه اندازه فایل (به بایت).
     */
    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0L
    }

    /**
     * بررسی اینکه آیا فایل خالی است یا خیر.
     */
    fun isFileEmpty(file: File): Boolean {
        return file.length() == 0L
    }

    /**
     * لیست کردن تمام فایل‌های موجود در یک پوشه.
     */
    fun listFilesInDirectory(directory: File): List<File> {
        return if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
}