package com.msa.core.data.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.net.URL

/**
 * یک شیء Singleton برای انجام کارهای کمکی مربوط به شبکه.
 */
object NetworkUtils {

    /**
     * بررسی وضعیت اتصال اینترنت.
     *
     * @param context کانتکست اپلیکیشن
     * @return true اگر اینترنت متصل باشد، false در غیر این صورت
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo?.isConnected == true
        }
    }

    /**
     * بررسی اتصال Wi-Fi.
     *
     * @param context کانتکست اپلیکیشن
     * @return true اگر به Wi-Fi متصل باشد، false در غیر این صورت
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }

    /**
     * بررسی اعتبار URL.
     *
     * @param url رشته URL
     * @return true اگر URL معتبر باشد، false در غیر این صورت
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
}