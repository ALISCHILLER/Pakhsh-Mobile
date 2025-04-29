package com.msa.core.di



import android.content.Context
import androidx.room.Room
import com.msa.core.config.AppConfig
import com.msa.core.data.database.BaseDatabase
import org.koin.dsl.module
import androidx.room.migration.Migration
import org.koin.dsl.module

/**
 * ماژول Base برای Room Database.
 */
inline fun <reified T : BaseDatabase> baseDatabaseModule(
    databaseClass: Class<T>,
    crossinline daoProvider: (T) -> Any, // اضافه کردن crossinline
    migrations: List<Migration> = emptyList()
) = module {

    // ارائه نمونه Singleton از دیتابیس
    single {
        BaseDatabase.getInstance(
            context = get(),
            databaseClass = databaseClass,
            databaseName = get<AppConfig>().databaseName,
            migrations = migrations
        )
    }

    // ارائه DAO
    factory { daoProvider(get()) }
}

//. نحوه استفاده
//val appDatabaseModule = baseDatabaseModule(
//    databaseClass = AppDatabase::class.java,
//    daoProvider = { it.userDao() }, // لامبدا بدون return غیرمحلی
//    migrations = listOf(
//        createMigration(1, 2) {
//            execSQL("ALTER TABLE users ADD COLUMN email TEXT")
//        }
//    )
//)