package com.zar.zarpakhsh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity

@Dao
interface CustomerDao {


     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insertCustomer(customer: CustomerModelEntity)

     @Query("SELECT * FROM customer WHERE id = :customerId")
     suspend fun getCustomerById(customerId: String): CustomerModelEntity?

     @Query("DELETE FROM customer")
     suspend fun deleteAllCustomers()
}