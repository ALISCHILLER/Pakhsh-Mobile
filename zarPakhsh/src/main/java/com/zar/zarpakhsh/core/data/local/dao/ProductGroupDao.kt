package com.zar.zarpakhsh.data.local.dao

import androidx.room.*
import com.zar.zarpakhsh.data.local.entity.ProductGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductGroupDao {

    // ✅ درج یک گروه محصول
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductGroup(productGroup: ProductGroupEntity)

    // ✅ درج چند گروه محصول
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productGroups: List<ProductGroupEntity>)

    // ✅ دریافت همه گروه‌های محصول
    @Query("SELECT * FROM product_groups")
    fun getAllProductGroups(): Flow<List<ProductGroupEntity>>

    // ✅ دریافت گروه محصول با شناسه
    @Query("SELECT * FROM product_groups WHERE id = :groupId LIMIT 1")
    suspend fun getProductGroupById(groupId: String): ProductGroupEntity?

    // ✅ حذف همه گروه‌های محصول
    @Query("DELETE FROM product_groups")
    suspend fun clearAll()

    // ✅ حذف یک گروه خاص
    @Delete
    suspend fun deleteProductGroup(productGroup: ProductGroupEntity)

    // ✅ جستجو در گروه‌ها براساس نام
    @Query("SELECT * FROM product_groups WHERE name LIKE '%' || :query || '%'")
    fun searchProductGroups(query: String): Flow<List<ProductGroupEntity>>
}
