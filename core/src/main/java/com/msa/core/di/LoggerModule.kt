package com.msa.core.di



import com.msa.core.utils.logger.*
import org.koin.dsl.module

/**
 * ماژول برای مدیریت وابستگی‌های لاگ‌گیری.
 */
val loggerModule = module {

    // ارائه FileLoggingTree به‌عنوان Singleton
    single { FileLoggingTree(get()) }

    // ارائه LoggerHelper به‌عنوان Singleton
    single {
        LoggerHelper.apply {
            init(get(), "error_log.txt") // تنظیمات اولیه لاگر
        }
    }
}