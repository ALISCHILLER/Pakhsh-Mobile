package com.zar.persistenc.data.signalR

import android.util.Log
import androidx.lifecycle.ViewModel
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class SignalRManager(private val hubUrl: String) : ViewModel(), CoroutineScope {

    private lateinit var hubConnection: HubConnection
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    private val _signalREvents = MutableSharedFlow<SignalREvent>(replay = 0)
    val signalREvents: SharedFlow<SignalREvent> get() = _signalREvents

    private val reconnectDelayMillis = 5000L

    var emitter: RemoteSignalREmitter? = null

    companion object {
        const val METHOD_RECEIVE_MESSAGE = "ReceiveMessage"
        const val METHOD_GET_POINT = "GetPoint"
        const val METHOD_PREVIOUS_STATION_REACHED = "PreviousStationReached"
        const val METHOD_SEND_LOCATION = "SendLocation"
    }

    fun startConnection() {
        launch {
            try {
                hubConnection = HubConnectionBuilder.create(hubUrl)
                    .withTransport(TransportEnum.LONG_POLLING)
                    .build()

                setupEventHandlers()
                connect()
            } catch (e: Exception) {
                Log.e("SignalR", "Error creating connection", e)
                emitError(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun connect() {
        withContext(Dispatchers.IO) {
            try {
                hubConnection.start().blockingAwait()
                emit(SignalREvent.Connected)
                emitter?.onConnectToSignalR()
            } catch (e: Exception) {
                Log.e("SignalR", "Connection failed", e)
                emitError(e.message ?: "Connection failed")
                emitter?.onErrorConnectToSignalR()
                retryReconnect()
            }
        }
    }

    private fun setupEventHandlers() {
        hubConnection.on(METHOD_RECEIVE_MESSAGE, { user: String, notification: NotificationModel ->
            launch {
                _signalREvents.emit(SignalREvent.MessageReceived(user, notification))
                emitter?.onReceiveMessage(user, notification)
            }
        }, String::class.java, NotificationModel::class.java)

        hubConnection.on(METHOD_GET_POINT, { lat: String, lng: String, visitorId: String ->
            launch {
                _signalREvents.emit(SignalREvent.PointReceived(lat, lng, visitorId))
                emitter?.onGetPoint(lat, lng, visitorId)
            }
        }, String::class.java, String::class.java, String::class.java)

        hubConnection.on(METHOD_PREVIOUS_STATION_REACHED, { message: String ->
            launch {
                _signalREvents.emit(SignalREvent.StationReached(message))
                emitter?.onPreviousStationReached(message)
            }
        }, String::class.java)

        hubConnection.onClosed { exception ->
            launch {
                if (exception != null) {
                    emitError(exception.message ?: "Connection closed unexpectedly")
                    emitter?.onErrorConnectToSignalR()
                } else {
                    emit(SignalREvent.Disconnected)
                }
                retryReconnect()
            }
        }
    }

    private fun retryReconnect() {
        launch {
            delay(reconnectDelayMillis)
            emit(SignalREvent.Reconnecting)
            emitter?.onReConnectToSignalR()
            connect()
        }
    }

    private fun emit(event: SignalREvent) {
        launch { _signalREvents.emit(event) }
    }

    private fun emitError(message: String) {
        emit(SignalREvent.Error(message))
    }

    fun sendMessageN(methodName: String, user: String, notification: NotificationModel) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            launch(Dispatchers.IO) {
                try {
                    hubConnection.send(methodName, user, notification)
                } catch (e: Exception) {
                    emitError("Send failed: ${e.message}")
                }
            }
        }
    }

    fun sendMessage(methodName: String, user: String, message: String) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            launch(Dispatchers.IO) {
                try {
                    hubConnection.send(methodName, user, message)
                } catch (e: Exception) {
                    emitError("Send failed: ${e.message}")
                }
            }
        }
    }

    fun sendVisitorLocation(torId: UUID?, lat: String?, lon: String?) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            launch(Dispatchers.IO) {
                try {
                    hubConnection.send("sendVisitorLocation", torId, lat, lon)
                } catch (e: Exception) {
                    emitError("Send failed: ${e.message}")
                }
            }
        }
    }

    fun VisitJoinGroup() {
        launch(Dispatchers.IO) {
            try {
                hubConnection.send("DistJoinGroup")
            } catch (e: Exception) {
                emitError("Error joining group: ${e.message}")
            }
        }
    }

    fun joinGroup(groupName: String) {
        launch(Dispatchers.IO) {
            try {
                hubConnection.invoke("JoinGroup", groupName).blockingAwait()
            } catch (e: Exception) {
                emitError("Error joining group: ${e.message}")
            }
        }
    }

    fun leaveGroup(groupName: String) {
        launch(Dispatchers.IO) {
            try {
                hubConnection.invoke("LeaveGroup", groupName).blockingAwait()
            } catch (e: Exception) {
                emitError("Error leaving group: ${e.message}")
            }
        }
    }

    fun stopConnection() {
        job.cancel()
        if (::hubConnection.isInitialized) {
            hubConnection.stop()
        }
    }
}