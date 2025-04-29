package com.msa.core.utils.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object NetworkHelper {

    private val client = HttpClient(Android)

    /**
     * چک کردن وضعیت اینترنت دستگاه
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // برای اندرویدهای 27 به بالا باید از متد جدیدتر getNetworkCapabilities استفاده کنید
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
        return false
//        else {
//            // برای نسخه‌های پایین‌تر از API 23 از activeNetworkInfo استفاده می‌شود
//            val networkInfo = connectivityManager.activeNetworkInfo
//            return networkInfo != null && networkInfo.isConnected
//        }
    }

    /**
     * ارسال درخواست GET به یک URL و دریافت نتیجه
     */
    suspend fun sendGetRequest(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.get(url)
                if (response.status == HttpStatusCode.OK) {
                    return@withContext response.bodyAsText()
                } else {
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    /**
     * ارسال درخواست POST به یک URL با بدنه JSON
     */
    suspend fun sendPostRequest(url: String, body: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                if (response.status == HttpStatusCode.OK) {
                    return@withContext  response.bodyAsText()
                } else {
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    /**
     * بررسی وضعیت اتصال به اینترنت با استفاده از یک URL خاص
     */
    suspend fun isServerReachable(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.head(url)
                return@withContext response.status == HttpStatusCode.OK
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    /**
     * تبدیل خطاها به پیام‌های قابل نمایش
     */
    fun parseError(error: Throwable): String {
        return when (error) {
            is IOException -> "اتصال به سرور برقرار نشد"
            else -> "خطای نامشخص"
        }
    }
}
