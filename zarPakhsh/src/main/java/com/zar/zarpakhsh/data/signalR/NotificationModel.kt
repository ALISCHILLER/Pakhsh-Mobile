package com.zar.zarpakhsh.data.signalR

data class NotificationModel(
    val id: String="",
    val title: String,
    val body: String="",
    val message: String,
    val timestamp: Long=0
)