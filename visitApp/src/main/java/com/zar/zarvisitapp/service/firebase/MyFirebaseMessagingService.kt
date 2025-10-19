package com.zar.visitApp.service.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import com.zar.zarpakhsh.data.local.dao.FCMMessageDao
import com.zar.zarpakhsh.data.local.entity.FCMMessageEntity
import com.zar.visitApp.MainActivity
import com.zar.visitApp.R
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    // Koin Inject
    private val fcmMessageDao: FCMMessageDao by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Token: $token")
        // توکن را به سرور بفرستید
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Log Data Payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data Payload: ${remoteMessage.data}")
            saveMessageToDb(remoteMessage)
        }

        // Notification Payload
        remoteMessage.notification?.let {
            sendNotification(it)
        }
    }

    private fun saveMessageToDb(remoteMessage: RemoteMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            val message = FCMMessageEntity(
                title = remoteMessage.notification?.title,
                body = remoteMessage.notification?.body,
                data = remoteMessage.data.toString()
            )
            fcmMessageDao.insert(message)
        }
    }

    private fun sendNotification(notification: RemoteMessage.Notification) {
        val channelId = "default_channel_id"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    // ارسال توکن به سرور شما (اختیاری ولی پیشنهادی)
    private fun sendRegistrationToServer(token: String) {
        // در اینجا می‌توانید توکن را به API سرورتان ارسال کنید
        // مثلاً با استفاده از Retrofit یا WorkManager
        Log.i(TAG, "Sending token to server: $token")
    }
}