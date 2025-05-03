package com.msa.zarpakhsh.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.zarpakhsh.data.local.dao.UserDao
import com.msa.zarpakhsh.data.local.entity.UserModelEntity


@Database(
    entities = [
        UserModelEntity::class
    ], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}