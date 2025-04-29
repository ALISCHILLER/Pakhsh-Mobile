package com.msa.core.data.database


import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * ابزارهای کمکی برای Room Database.
 */
object DatabaseUtils {

    /**
     * ایجاد یک نمونه جدید از دیتابیس.
     */
    inline fun <reified T : RoomDatabase> createDatabase(
        context: Context,
        databaseName: String
    ): T {
        return Room.databaseBuilder(
            context.applicationContext,
            T::class.java,
            databaseName
        ).build()
    }

    /**
     * ایجاد یک نمونه جدید از دیتابیس با Migration.
     */
    inline fun <reified T : RoomDatabase> createDatabaseWithMigration(
        context: Context,
        databaseName: String,
        vararg migrations: Migration
    ): T {
        return Room.databaseBuilder(
            context.applicationContext,
            T::class.java,
            databaseName
        ).addMigrations(*migrations).build()
    }

    /**
     * حذف دیتابیس به‌صورت کامل.
     */
    fun deleteDatabase(context: Context, databaseName: String) {
        context.deleteDatabase(databaseName)
    }

    /**
     * Migration نمونه‌ای برای ارتقاء دیتابیس.
     */
    fun createMigration(startVersion: Int, endVersion: Int, migrationBlock: SupportSQLiteDatabase.() -> Unit): Migration {
        return object : Migration(startVersion, endVersion) {
            override fun migrate(database: SupportSQLiteDatabase) {
                migrationBlock.invoke(database)
            }
        }
    }

    /**
     * اجرای یک بلاک کد به‌صورت امن (با مدیریت خطا).
     */
    inline fun <T> safeExecute(block: () -> T, onErrorReturn: T): T {
        return try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            onErrorReturn
        }
    }
}

//نحوه استفاده
//
//ایجاد دیتابیس
//
//val db = DatabaseUtils.createDatabase<AppDatabase>(context, "app_database")
//ایجاد دیتابیس با Migration
//val migration1to2 = DatabaseUtils.createMigration(1, 2) {
//    execSQL("ALTER TABLE users ADD COLUMN email TEXT")
//}
//
//val db = DatabaseUtils.createDatabaseWithMigration<AppDatabase>(
//    context,
//    "app_database",
//    migration1to2
//)
//حذف دیتابیس
//DatabaseUtils.deleteDatabase(context, "app_database")
//
//اجرای عملیات امن
//val result = DatabaseUtils.safeExecute(
//    block = { userDao.getAllUsers() },
//    onErrorReturn = emptyList()
//)
//
//مثال کامل: Migration
//فرض کنید نیاز دارید ستون جدیدی به جدول users اضافه کنید. می‌توانید از این Migration استفاده کنید:
//
//val migration1to2 = DatabaseUtils.createMigration(1, 2) {
//    execSQL("ALTER TABLE users ADD COLUMN email TEXT")
//}
//
//val db = DatabaseUtils.createDatabaseWithMigration<AppDatabase>(
//    context,
//    "app_database",
//    migration1to2
//)