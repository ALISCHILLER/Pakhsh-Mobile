package com.msa.persistence.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.*
import com.msa.core.common.config.AppConfig
import com.msa.core.storage.prefs.BaseSharedPreferences
import com.msa.persistence.R
import com.msa.persistence.data.signalR.NotificationModel
import com.msa.persistence.data.signalR.SignalRManager // مقصد نوتی
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class LiveLocationService : Service(), KoinComponent {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var request: LocationRequest

    // بهتره از DI بگیری (Koin/Hilt). فعلاً مستقیم:
    private lateinit var signalRManager: SignalRManager
    private val appConfig: AppConfig by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Throttle برای جلوگیری از ارسال‌های خیلی پُر‌تعداد
    private val lastSentAt = AtomicLong(0L)
    private val minSendIntervalMs = 3_000L

    // هویت پایدار دستگاه/کاربر (از prefs یا توکن)
    private val prefs: BaseSharedPreferences by inject()
    private val deviceId by lazy {
        // اگر آیدی کاربر دارید، جایگزین کنید. در غیر این صورت یک آیدی پایدار بساز/نگه‌دار
        prefs.getStringOrNull("device_id") ?: UUID.randomUUID().toString().also {
            prefs.saveString("device_id", it)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("LiveLocationService onCreate")

        // Fused Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        signalRManager = SignalRManager(appConfig.signalRUrl)
        serviceScope.launch { signalRManager.startConnection() }

        // Location Request (Android 12+ هم اوکی)
        request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L // هر 5 ثانیه
        )
            .setMinUpdateDistanceMeters(10f)
            .build()

        // Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return
                handleLocation(loc)
            }
        }

        // Start Foreground
        startForegroundWithNotification()

        // Location updates (permission check)
        startLocationUpdates()
    }

    private fun handleLocation(location: Location) {
        val now = System.currentTimeMillis()
        if (now - lastSentAt.get() < minSendIntervalMs) return
        lastSentAt.set(now)

        val (lat, lng) = location.latitude to location.longitude
        val notif = NotificationModel(
            id = UUID.randomUUID().toString(),
            title = "Live Location",
            body = "Lat: $lat, Lng: $lng",
            message = "Updated",
            timestamp = now
        )

        // ارسال در IO
        serviceScope.launch {
            runCatching {
                signalRManager.sendMessageN(
                    SignalRManager.METHOD_SEND_LOCATION,
                    deviceId,
                    notif
                )
                Timber.d("Location sent: $lat, $lng")
            }.onFailure {
                Timber.e(it, "Failed to send location")
            }
        }
    }

    private fun startLocationUpdates() {
        // قبل از درخواست باید دسترسی‌ها را چک کنیم
        val fineGranted = ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            Timber.w("Location permission not granted; stopping service")
            stopSelf()
            return
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }

    private fun startForegroundWithNotification() {
        val channelId = "location_channel"
        createChannelIfNeeded(channelId, "Live Location")

        // وقتی کاربر روی نوتی کلیک می‌کند، وارد اپ شود
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or maybeImmutable(),
            )
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("در حال ارسال موقعیت مکانی…")
            .setSmallIcon(R.drawable.ic_stat_location) // آیکن 24dp تک‌رنگ
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        contentIntent?.let { builder.setContentIntent(it) }

        val notif: Notification = builder.build()

        // از ServiceCompat با نوع لوکیشن استفاده کن (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                1,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notif)
        }
    }

    private fun createChannelIfNeeded(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            mgr.createNotificationChannel(channel)
        }
    }

    private fun maybeImmutable(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // اگر نیاز بود، دستور start/stop خاص هندل کن
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("LiveLocationService onDestroy")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.launch { runCatching { signalRManager.stopConnection() } }
        serviceScope.cancel()
    }
}
