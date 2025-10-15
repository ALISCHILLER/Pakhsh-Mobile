package com.zar.zarpakhsh.data.signalR

sealed class SignalREvent {
    object Connected : SignalREvent()
    object Disconnected : SignalREvent()
    object Reconnecting : SignalREvent()
    data class Error(val message: String) : SignalREvent()
    data class MessageReceived(val user: String, val notification: NotificationModel) : SignalREvent()
    data class PointReceived(val lat: String, val lng: String, val visitorId: String) : SignalREvent()
    data class StationReached(val message: String) : SignalREvent()
}