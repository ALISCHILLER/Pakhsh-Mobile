package com.msa.core.di


import android.content.Context
import com.google.gson.Gson
import com.msa.core.config.AppConfig
import com.msa.core.data.storage.BaseSharedPreferences
import com.msa.core.utils.Currency
import com.msa.core.utils.EnhancedNumberConverter
import com.msa.core.utils.file.FileManager
import com.msa.core.utils.logger.FileLoggingTree
import com.msa.core.utils.logger.LoggerHelper
import com.msa.core.utils.validation.NationalCodeValidator
import org.koin.dsl.module

val utilsModule = module {
    single { EnhancedNumberConverter }
    factory { Currency.of(0) } // مقدار پیش‌فرض صفر
    single { NationalCodeValidator } // مقدار پیش‌فرض صفر

    single { FileManager(get()) }

    single { Gson() }

    // ارائه BaseSharedPreferences به‌عنوان Singleton
    single {
        BaseSharedPreferences(
            context = get(),
            prefsName = get<AppConfig>().sharedPreferencesName,
            isEncrypted = true, // استفاده از EncryptedSharedPreferences
            gson = get()
        )
    }
    single {
        val fileManager: FileManager = get()
        FileLoggingTree(fileManager.getOrCreateFile("logs", "error_log.txt"))
    }


    single {
        val context = get<Context>()
        val tree = get<FileLoggingTree>()
        LoggerHelper.apply {
            init(context = context, tree = tree)
        }
    }
}