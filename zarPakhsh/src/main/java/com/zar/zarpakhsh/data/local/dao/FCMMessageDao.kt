package com.zar.zarpakhsh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zar.zarpakhsh.data.local.entity.FCMMessageEntity

@Dao
interface FCMMessageDao {
    @Insert
    suspend fun insert(message: FCMMessageEntity)

    @Query("SELECT * FROM fcm_messages ORDER BY timestamp DESC LIMIT 100")
    suspend fun getAllMessages(): List<FCMMessageEntity>
}