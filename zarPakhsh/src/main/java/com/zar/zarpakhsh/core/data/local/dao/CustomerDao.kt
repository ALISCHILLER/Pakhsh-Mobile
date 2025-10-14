package com.zar.zarpakhsh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

     // --- Insert ---
     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertCustomer(customer: CustomerModelEntity)

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertAll(customers: List<CustomerModelEntity>)

     // --- Query ---
     @Query("SELECT * FROM customers")
     fun getAllAsFlow(): Flow<List<CustomerModelEntity>>

     @Query("SELECT * FROM customers WHERE id = :customerId")
     suspend fun getCustomerById(customerId: String): CustomerModelEntity?

     @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR mobile LIKE '%' || :query || '%'")
     fun searchCustomers(query: String): Flow<List<CustomerModelEntity>>

     @Query("SELECT COUNT(*) FROM customers")
     suspend fun getCustomerCount(): Int

     // --- Delete ---
     @Query("DELETE FROM customers")
     suspend fun deleteAllCustomers()

     // --- Transactional Operations ---
     @Transaction
     suspend fun refreshCustomers(customers: List<CustomerModelEntity>) {
          deleteAllCustomers()
          insertAll(customers)
     }
}