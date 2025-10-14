package com.zar.zarpakhsh.data.local.dao

import androidx.room.*
import com.zar.zarpakhsh.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ✅ درج یک محصول (جایگزینی در صورت تکرار)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // ✅ درج لیست محصولات
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    // ✅ دریافت همه محصولات
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    // ✅ دریافت یک محصول با شناسه
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    // ✅ حذف همه محصولات
    @Query("DELETE FROM products")
    suspend fun clearAll()

    // ✅ حذف محصول خاص
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // ✅ جستجو براساس نام یا دسته‌بندی
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>
}
