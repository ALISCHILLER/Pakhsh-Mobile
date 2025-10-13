package com.zar.core.di



import com.zar.core.config.AppConfig
import com.zar.core.data.database.BaseDatabase
import org.koin.dsl.module
import androidx.room.migration.Migration

/**
 * ماژول Base برای Room Database.
 */
inline fun <reified T : BaseDatabase> baseDatabaseModule(
    databaseClass: Class<T>,
    crossinline daoProvider: (T) -> Any, // اضافه کردن crossinline
    migrations: List<Migration> = emptyList(),
    useDestructiveMigration: Boolean = false
) = module {

    // ارائه نمونه Singleton از دیتابیس
    single {
        BaseDatabase.getInstance(
            context = get(),
            databaseClass = databaseClass,
            databaseName = get<AppConfig>().databaseName,
            migrations = migrations,
            useDestructiveMigration = useDestructiveMigration
        )
    }

    // ارائه DAO
    factory { daoProvider(get()) }
}

