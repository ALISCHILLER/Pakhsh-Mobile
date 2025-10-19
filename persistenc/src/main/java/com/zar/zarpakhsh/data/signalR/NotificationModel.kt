package com.zar.persistenc.data.signalR

data class NotificationModel(
    val id: String="",
    val title: String,
    val body: String="",
    val message: String,
    val timestamp: Long=0
)