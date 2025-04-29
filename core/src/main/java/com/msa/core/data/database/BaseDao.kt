package com.msa.core.data.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
/**
 * کلاس پایه برای DAO.
 */
interface BaseDao<T> {

    /**
     * درج یک یا چند آیتم.
     */
    @Insert
    suspend fun insert(vararg entity: T)

    /**
     * به‌روزرسانی یک یا چند آیتم.
     */
    @Update
    suspend fun update(vararg entity: T)

    /**
     * حذف یک یا چند آیتم.
     */
    @Delete
    suspend fun delete(vararg entity: T)
}