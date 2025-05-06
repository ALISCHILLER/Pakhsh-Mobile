package com.zar.core.data.network.handler

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.error.*
import com.zar.core.data.network.model.ApiResponse
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * NetworkHandler: مدیریت تمام درخواست‌های API و خطاهای آن‌ها
 */
object NetworkHandler {

    public lateinit var appContext: Context
    private lateinit var networkMonitor: NetworkStatusMonitor
    lateinit var client: HttpClient

    /**
     * راه‌اندازی NetworkHandler
     */
    fun initialize(context: Context, monitor: NetworkStatusMonitor, httpClient: HttpClient) {
        appContext = context.applicationContext
        networkMonitor = monitor
        client = httpClient
    }

    /**
     * متد عمومی برای انجام درخواست‌های API با مدیریت خطا
     */
    suspend inline fun <reified T> safeApiCall(
        requireConnection: Boolean = true,
        noinline apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return try {
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

            withContext(Dispatchers.IO) {
                val response = apiCall()
                handleApiResponse(response)
            }

        } catch (e: Exception) {
            Timber.e(e, "API call failed: ${e.localizedMessage}")
            NetworkResult.Error.fromException(e, appContext)
        }
    }

    /**
     * پردازش پاسخ API و تبدیل به NetworkResult
     */
    public fun <T> handleApiResponse(response: ApiResponse<T>): NetworkResult<T> {
        return if (!response.hasError) {
            response.data?.let { data ->
                NetworkResult.Success(data)
            } ?: NetworkResult.Error(ParsingError(errorCode = "empty_data", message = "No data in response"))
        } else {
            NetworkResult.Error(ApiError(
                errorCode = response.code.toString(),
                message = response.message ?: "Server error occurred",
                statusCode = response.code ?: -1
            ))
        }
    }

    /**
     * بررسی وضعیت اتصال شبکه
     */
    public suspend fun hasNetworkConnection(): Boolean {
        return when (networkMonitor.networkStatus.first()) {
            is NetworkStatusMonitor.NetworkStatus.Available -> true
            else -> false
        }
    }

    // ————————————————————————————————
    // ✅ متدهای HTTP جدید
    // ————————————————————————————————

    suspend inline fun <reified T> get(url: String): NetworkResult<T> =
        safeApiCall { client.get(url).body() }

    suspend inline fun <reified T> post(url: String, body: Any): NetworkResult<T> =
        safeApiCall { client.post(url) { setBody(body) }.body() }

    suspend inline fun <reified T> put(url: String, body: Any): NetworkResult<T> =
        safeApiCall { client.put(url) { setBody(body) }.body() }

    suspend inline fun <reified T> delete(url: String): NetworkResult<T> =
        safeApiCall { client.delete(url).body() }

    suspend inline fun <reified T> patch(url: String, body: Any): NetworkResult<T> =
        safeApiCall { client.patch(url) { setBody(body) }.body() }

    suspend inline fun <reified T> head(url: String): NetworkResult<T> =
        safeApiCall { client.head(url).body() }
}