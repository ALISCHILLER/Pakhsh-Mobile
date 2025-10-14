package com.zar.zarpakhsh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zar.zarpakhsh.data.local.entity.FCMMessageEntity

@Dao
interface FCMMessageDao {

    @Insert
    suspend fun insert(message: FCMMessageEntity)

    @Query("SELECT * FROM fcm_messages ORDER BY receivedAt DESC LIMIT :limit")
    suspend fun getAllMessages(limit: Int = 100): List<FCMMessageEntity>

    @Query("SELECT * FROM fcm_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): FCMMessageEntity?

    @Query("SELECT * FROM fcm_messages WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY receivedAt DESC")
    suspend fun searchMessages(query: String): List<FCMMessageEntity>

    @Query("DELETE FROM fcm_messages WHERE receivedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM fcm_messages")
    suspend fun deleteAll()

    @Update
    suspend fun update(message: FCMMessageEntity)

    // Optional Extensions
    @Query("UPDATE fcm_messages SET isRead = 1 WHERE id IN (:ids)")
    suspend fun markAsRead(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM fcm_messages WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
}