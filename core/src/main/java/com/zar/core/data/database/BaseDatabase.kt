package com.zar.core.data.database



import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * کلاس پایه برای مدیریت Room Database.
 */
abstract class BaseDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: BaseDatabase? = null

        /**
         * دریافت نمونه Singleton از دیتابیس.
         */
        fun <T : BaseDatabase> getInstance(
            context: Context,
            databaseClass: Class<T>,
            databaseName: String,
            migrations: List<androidx.room.migration.Migration> = emptyList(),
            useDestructiveMigration: Boolean = false
        ): T {
            return INSTANCE as? T ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    databaseClass,
                    databaseName
                ).apply {
                    if (migrations.isNotEmpty()) {
                        addMigrations(*migrations.toTypedArray())
                    } else if (useDestructiveMigration) {
                        fallbackToDestructiveMigration()
                    }
                }.build()
                INSTANCE = instance
                instance as T
            }
        }
    }
}

