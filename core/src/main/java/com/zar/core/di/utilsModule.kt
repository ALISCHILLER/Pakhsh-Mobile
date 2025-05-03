package com.zar.core.di

import com.google.gson.Gson
import com.zar.core.config.AppConfig
import com.zar.core.data.storage.BaseSharedPreferences
import com.zar.core.ui.camera.CameraHelper
import com.zar.core.utils.Currency
import com.zar.core.utils.EnhancedNumberConverter
import com.zar.core.utils.file.FileManager
import com.zar.core.utils.logger.FileLoggingTree
import com.zar.core.utils.logger.LoggerHelper
import com.zar.core.utils.validation.NationalCodeValidator
import org.koin.dsl.module

val utilsModule = module {
    single { EnhancedNumberConverter }
    single { Currency(0) } // مقدار پیش‌فرض صفر
    single { NationalCodeValidator } // مقدار پیش‌فرض صفر

    single { FileManager(get()) }

    // ارائه Gson به‌عنوان Singleton
    single { lazy { Gson() } }

    // ارائه BaseSharedPreferences به‌عنوان Singleton
    single {
        BaseSharedPreferences(
            context = get(),
            prefsName = get<AppConfig>().sharedPreferencesName,
            isEncrypted = true, // استفاده از EncryptedSharedPreferences
            gson = get()
        )
    }
    // ارائه FileLoggingTree به‌عنوان Singleton
    single { FileLoggingTree(get()) }

    // ارائه LoggerHelper به‌عنوان Singleton
    single {
        LoggerHelper.apply {
            init(get(), "error_log.txt") // تنظیمات اولیه لاگر
        }
    }
    single { CameraHelper(get()) }
}