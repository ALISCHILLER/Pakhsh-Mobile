package com.zar.zarpakhsh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fcm_messages")
data class FCMMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String?,
    val body: String?,
    val data: String?,
    val timestamp: Long = System.currentTimeMillis()
)