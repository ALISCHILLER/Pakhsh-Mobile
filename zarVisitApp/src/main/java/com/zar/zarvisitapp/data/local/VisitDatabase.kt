// zarvisitapp/src/main/java/com/zar/zarvisitapp/data/local/VisitDatabase.kt

package com.zar.zarvisitapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zar.zarpakhsh.data.local.dao.CustomerDao
import com.zar.zarpakhsh.data.local.dao.FCMMessageDao
import com.zar.zarpakhsh.data.local.dao.ProductDao
import com.zar.zarpakhsh.data.local.dao.ProductGroupDao
import com.zar.zarpakhsh.data.local.dao.ProductUnitDao
import com.zar.zarpakhsh.data.local.dao.UserDao
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import com.zar.zarpakhsh.data.local.entity.FCMMessageEntity
import com.zar.zarpakhsh.data.local.entity.ProductEntity
import com.zar.zarpakhsh.data.local.entity.ProductGroupEntity
import com.zar.zarpakhsh.data.local.entity.ProductUnitEntity
import com.zar.zarpakhsh.data.local.entity.UserModelEntity
import com.zar.zarvisitapp.data.local.dao.VisitDao
import com.zar.zarvisitapp.data.local.entities.VisitEntity

@Database(
    entities = [
        UserModelEntity::class, // ✅ مشترک
        VisitEntity::class, // ✅ اختصاصی
        CustomerModelEntity::class,
        ProductEntity::class,
        ProductGroupEntity::class,
        ProductUnitEntity::class,
        FCMMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VisitDatabase : RoomDatabase() {


    abstract fun userDao(): UserDao
    abstract fun visitDao(): VisitDao
    abstract fun productGroupDao(): ProductGroupDao
    abstract fun productDao(): ProductDao
    abstract fun productUnitDao(): ProductUnitDao
    abstract fun customerDao(): CustomerDao
    abstract fun fcmMessageDao(): FCMMessageDao

//    companion object {
//        private const val DB_NAME = "visit_database"
//
//
//        fun create(context: Context): VisitDatabase =
//            Room.databaseBuilder(context, VisitDatabase::class.java, DB_NAME)
//                .fallbackToDestructiveMigration()
//                .build()
//    }
}