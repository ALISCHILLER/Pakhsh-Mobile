package com.zar.zarpakhsh.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.zar.zarpakhsh.data.signalR.NotificationModel
import com.zar.zarpakhsh.data.signalR.SignalRManager

import java.util.*

class LiveLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var signalRManager: SignalRManager

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        signalRManager = SignalRManager("https://your-signalr-url")
        signalRManager.startConnection()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                sendLocationToServer(location.latitude, location.longitude)
            }
        }

        startForegroundWithNotification()
        startLocationUpdates()
    }

    private fun sendLocationToServer(lat: Double, lng: Double) {
        val fakeUser = "device_${UUID.randomUUID()}"
        val notification = NotificationModel(
            id = UUID.randomUUID().toString(),
            title = "Live Location",
            body = "Lat: $lat, Lng: $lng",
            message = "Updated",
            timestamp = System.currentTimeMillis()
        )
        signalRManager.sendMessageN(SignalRManager.METHOD_SEND_LOCATION, fakeUser, notification)
        Log.d("LiveLocationService", "Location sent: $lat, $lng")
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    private fun startForegroundWithNotification() {
        val channelId = "location_channel"
        val channel = NotificationChannel(channelId, "Live Location", NotificationManager.IMPORTANCE_LOW)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking location")
            .setContentText("Sending live location to server...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        signalRManager.stopConnection()
    }
}