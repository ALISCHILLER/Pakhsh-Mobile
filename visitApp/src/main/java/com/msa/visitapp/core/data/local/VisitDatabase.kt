// visitapp/src/main/java/com/msa/visitapp/data/local/VisitDatabase.kt

package com.msa.visitapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.msapakhsh.data.local.dao.CustomerDao
import com.msa.msapakhsh.data.local.dao.FCMMessageDao
import com.msa.msapakhsh.data.local.dao.ProductDao
import com.msa.msapakhsh.data.local.dao.ProductGroupDao
import com.msa.msapakhsh.data.local.dao.ProductUnitDao
import com.msa.msapakhsh.data.local.dao.UserDao
import com.msa.msapakhsh.data.local.entity.CustomerModelEntity
import com.msa.msapakhsh.data.local.entity.FCMMessageEntity
import com.msa.msapakhsh.data.local.entity.ProductEntity
import com.msa.msapakhsh.data.local.entity.ProductGroupEntity
import com.msa.msapakhsh.data.local.entity.ProductUnitEntity
import com.msa.msapakhsh.data.local.entity.UserModelEntity
import com.msa.visitapp.core.data.local.dao.VisitDao
import com.msa.visitapp.core.data.local.entities.VisitEntity

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