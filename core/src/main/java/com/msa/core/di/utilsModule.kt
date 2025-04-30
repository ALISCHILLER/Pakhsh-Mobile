package com.msa.core.di

import com.google.gson.Gson
import com.msa.core.config.AppConfig
import com.msa.core.data.storage.BaseSharedPreferences
import com.msa.core.ui.camera.CameraHelper
import com.msa.core.utils.Currency
import com.msa.core.utils.EnhancedNumberConverter
import com.msa.core.utils.file.FileManager
import com.msa.core.utils.logger.FileLoggingTree
import com.msa.core.utils.logger.LoggerHelper
import com.msa.core.utils.logger.LoggerHelper.init
import com.msa.core.utils.validation.NationalCodeValidator
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