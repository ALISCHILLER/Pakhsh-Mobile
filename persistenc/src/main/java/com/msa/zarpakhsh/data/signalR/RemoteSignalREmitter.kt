package com.msa.persistenc.data.signalR

interface RemoteSignalREmitter {
    fun onConnectToSignalR()
    fun onErrorConnectToSignalR()
    fun onReConnectToSignalR()
    fun onReceiveMessage(user: String, message: NotificationModel)
    fun onGetPoint(lat: String, lng: String, visitorId: String)
    fun onPreviousStationReached(message: String)
}