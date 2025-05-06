package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.error.*
import com.zar.core.data.network.model.ApiResponse
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber

object NetworkHandler {

    private lateinit var appContext: Context
    private lateinit var networkMonitor: NetworkStatusMonitor
    private lateinit var client: HttpClient

    /**
     * این متد برای راه‌اندازی اجزای اصلی مانند context، network monitor و httpClient استفاده می‌شود
     */
    fun initialize(
        context: Context,
        monitor: NetworkStatusMonitor,
        httpClient: HttpClient
    ) {
        appContext = context.applicationContext
        networkMonitor = monitor
        client = httpClient
    }

    /**
     * متد safeApiCall برای مدیریت درخواست‌های API با مدیریت خطا
     */
    suspend fun <T> safeApiCall(
        requireConnection: Boolean = true,
        apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return try {
            // بررسی اتصال شبکه
            if (requireConnection && !hasNetworkConnection()) {
                Timber.e("No network connection available")
                return NetworkResult.Error(
                    error = ConnectionError(
                        errorCode = "network_unavailable",
                        message = appContext.getString(R.string.error_no_connection),
                        connectionType = null
                    )
                )
            }

            // اجرای درخواست API
            withContext(Dispatchers.IO) {
                val response = apiCall()
                handleApiResponse(response)
            }

        } catch (e: Exception) {
            Timber.e(e, "API call failed: ${e.localizedMessage}")
            // مدیریت خطاهای عمومی
            return NetworkResult.Error.fromException(e, appContext)
        }
    }

    /**
     * متد برای پردازش پاسخ API و مدیریت خطاها
     */
    private fun <T> handleApiResponse(response: ApiResponse<T>): NetworkResult<T> {
        return if (!response.hasError) {
            response.data?.let { data ->
                NetworkResult.Success(data)
            } ?: NetworkResult.Error(
                ParsingError(
                    errorCode = "empty_data",
                    message = "No data in response"
                )
            )
        } else {
            NetworkResult.Error(
                ApiError(
                    errorCode = response.code.toString(),
                    message = response.message ?: "Server error occurred",
                    statusCode = response.code ?: -1
                )
            )
        }
    }

    /**
     * بررسی اتصال شبکه
     */
    private suspend fun hasNetworkConnection(): Boolean {
        return when (networkMonitor.networkStatus.first()) {
            is NetworkStatusMonitor.NetworkStatus.Available -> true
            else -> false
        }
    }
}
