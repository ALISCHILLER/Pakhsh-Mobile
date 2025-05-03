package com.msa.zarpakhsh.di

import androidx.room.Room
import com.msa.zarpakhsh.data.local.AppDatabase
import com.msa.zarpakhsh.data.local.dao.UserDao
import org.koin.dsl.module

val dataLocalModule = module {
    // 1. ارائه AppDatabase به‌عنوان Singleton
    single<AppDatabase> {
        Room.databaseBuilder(
            get(), // Context از Koin دریافت می‌شود
            AppDatabase::class.java,
            "app_database" // نام فایل پایگاه داده
        )
            .fallbackToDestructiveMigration(false) // در صورت تغییر نسخه، داده‌ها حذف و مجدد ساخته می‌شوند
            .build()
    }

    // 2. ارائه UserDao
    single<UserDao> {
        val database: AppDatabase = get()
        database.userDao()
    }
}