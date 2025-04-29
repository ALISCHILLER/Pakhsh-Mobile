package com.msa.core.di
import com.google.gson.Gson
import com.msa.core.config.AppConfig
import com.msa.core.data.storage.BaseSharedPreferences
import com.msa.core.ui.camera.CameraHelper
import com.msa.core.utils.Currency
import com.msa.core.utils.EnhancedNumberConverter
import com.msa.core.utils.file.FileManager
import com.msa.core.utils.validation.NationalCodeValidator
import org.koin.dsl.module

val utilsModule = module {
    single { EnhancedNumberConverter }
    single { Currency(0) } // مقدار پیش‌فرض صفر
    single { NationalCodeValidator } // مقدار پیش‌فرض صفر

    single { FileManager(get()) }

    // ارائه Gson به‌عنوان Singleton
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

    single { CameraHelper(get()) }
}