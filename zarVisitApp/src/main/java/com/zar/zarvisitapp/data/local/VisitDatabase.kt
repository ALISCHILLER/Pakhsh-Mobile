// zarvisitapp/src/main/java/com/zar/zarvisitapp/data/local/VisitDatabase.kt

package com.zar.zarvisitapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zar.zarpakhsh.data.local.dao.UserDao
import com.zar.zarpakhsh.data.local.entity.UserModelEntity
import com.zar.zarvisitapp.data.local.dao.VisitDao
import com.zar.zarvisitapp.data.local.entities.VisitEntity

@Database(
    entities = [
        UserModelEntity::class, // ✅ مشترک
        VisitEntity::class      // ✅ اختصاصی
    ],
    version = 1,
    exportSchema = false
)
abstract class VisitDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun visitDao(): VisitDao

    companion object {
        private const val DB_NAME = "visit_database"

        fun create(context: Context): VisitDatabase =
            Room.databaseBuilder(context, VisitDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}