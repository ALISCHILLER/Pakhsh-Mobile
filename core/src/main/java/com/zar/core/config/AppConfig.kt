package com.zar.core.config

/**
 * Interface برای تنظیمات اپلیکیشن.
 * پیشنهاد: اگر بعداً خواستی baseUrl هم تزریق کنی، اینجا اضافه‌اش کن.
 */
interface AppConfig {
    val appFlavor: String
    val sharedPreferencesName: String
    val databaseName: String
    val signalRUrl: String
    // val baseUrl: String // (اختیاری) اگر نیاز شد برای Ktor
}
