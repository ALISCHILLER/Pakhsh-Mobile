package com.msa.core.data.database



import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.msa.core.config.AppConfig

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
            migrations: List<androidx.room.migration.Migration> = emptyList()
        ): T {
            return INSTANCE as? T ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    databaseClass,
                    databaseName
                ).apply {
                    if (migrations.isNotEmpty()) {
                        addMigrations(*migrations.toTypedArray())
                    }
                }.fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance as T
            }
        }
    }
}

//. نحوه استفاده

//@Database(entities = [UserEntity::class], version = 1)
//abstract class AppDatabase : BaseDatabase() {
//    abstract fun userDao(): UserDao
//}
//
//// Entity
//@Entity(tableName = "users")
//data class UserEntity(
//    @PrimaryKey(autoGenerate = true) val id: Long = 0,
//    val name: String,
//    val age: Int
//)
//
//// DAO
//@Dao
//interface UserDao : BaseDao<UserEntity> {
//    @Query("SELECT * FROM users WHERE id = :userId")
//    suspend fun getUserById(userId: Long): UserEntity?
//
//    @Query("SELECT * FROM users")
//    suspend fun getAllUsers(): List<UserEntity>
//}