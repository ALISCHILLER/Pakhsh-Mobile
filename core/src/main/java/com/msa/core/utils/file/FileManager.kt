package com.msa.core.utils.file


import android.content.Context
import java.io.File
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
     * ساخت یا بازگرداندن یک فایل.
     */
    fun getOrCreateFile(directoryName: String, fileName: String): File {
        val directory = getOrCreateDirectory(directoryName)
        return File(directory, fileName).apply {
            if (!exists()) {
                createNewFile()
            }
        }
    }

    /**
     * نوشتن متن به فایل.
     */
    @Throws(IOException::class)
    fun writeToFile(file: File, content: String, append: Boolean = false) {
        file.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.write(content)
            if (append) {
                writer.append("\n")
            }
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
     * بررسی وجود فایل.
     */
    fun fileExists(directoryName: String, fileName: String): Boolean {
        val file = File(getOrCreateDirectory(directoryName), fileName)
        return file.exists()
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


//نحوه استفاده

//private val fileManager: FileManager by inject()
//
//fun manageFiles() {
//    // ساخت یا بازگرداندن یک فایل
//    val file = fileManager.getOrCreateFile("logs", "example.txt")
//
//    // نوشتن متن به فایل
//    fileManager.writeToFile(file, "Hello, World!")
//
//    // خواندن محتویات فایل
//    val content = fileManager.readFile(file)
//    println(content)
//
//    // حذف فایل
//    fileManager.deleteFile("logs", "example.txt")
//}

//val fileManager: FileManager by inject()
//var fileContent by remember { mutableStateOf("") }
//val file = fileManager.getOrCreateFile("logs", "example.txt")
//fileManager.writeToFile(file, "Sample Content")
//fileContent = fileManager.readFile(file)