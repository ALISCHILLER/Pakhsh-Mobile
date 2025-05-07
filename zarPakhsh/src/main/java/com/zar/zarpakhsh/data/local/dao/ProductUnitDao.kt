package com.zar.zarpakhsh.data.local.dao

import androidx.room.*
import com.zar.zarpakhsh.data.local.entity.ProductUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductUnitDao {

    // ✅ درج یک واحد محصول
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductUnit(productUnit: ProductUnitEntity)

    // ✅ درج چند واحد محصول
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productUnits: List<ProductUnitEntity>)

    // ✅ دریافت همه واحدهای محصول
    @Query("SELECT * FROM product_units")
    fun getAllProductUnits(): Flow<List<ProductUnitEntity>>

    // ✅ دریافت واحد محصول با شناسه
    @Query("SELECT * FROM product_units WHERE id = :unitId LIMIT 1")
    suspend fun getProductUnitById(unitId: String): ProductUnitEntity?

    // ✅ حذف همه واحدها
    @Query("DELETE FROM product_units")
    suspend fun clearAll()

    // ✅ حذف یک واحد خاص
    @Delete
    suspend fun deleteProductUnit(productUnit: ProductUnitEntity)

    // ✅ جستجو در واحدهای محصول براساس نام
    @Query("SELECT * FROM product_units WHERE name LIKE '%' || :query || '%'")
    fun searchProductUnits(query: String): Flow<List<ProductUnitEntity>>
}
