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

    fun initialize(context: Context, monitor: NetworkStatusMonitor) {
        appContext = context.applicationContext
        networkMonitor = monitor
        client = createHttpClient("https://api.example.com")
    }

    private fun createHttpClient(baseUrl: String): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
        defaultRequest {
            url(baseUrl)
        }
    }

    suspend fun <T> safeApiCall(
        requireConnection: Boolean = true,
        apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return try {
            if (requireConnection && !hasNetworkConnection()) {
                return NetworkResult.Error(
                    error = ConnectionError(
                        message = appContext.getString(com.zar.core.R.string.error_no_connection),
                        cause = null,
                        connectionType = null
                    )
                )
            }
            withContext(Dispatchers.IO) {
                val response = apiCall()
                handleApiResponse(response)
            }
        } catch (e: Exception) {
            Timber.e(e, "API call failed")
            NetworkResult.Error.fromException(e, appContext)
        }
    }

    private fun <T> handleApiResponse(response: ApiResponse<T>): NetworkResult<T> {
        return if (!response.hasError) {
            response.data?.let { NetworkResult.Success(it) } ?: NetworkResult.Error(
                ConnectionError(
                    message = "Empty data",
                    cause = null,
                    connectionType = null
                )
            )
        } else {
            NetworkResult.Error(
                ApiError(
                    errorCode = response.code.toString(),
                    message = response.message ?: "Server error",
                    cause = null
                )
            )
        }
    }

    private suspend fun hasNetworkConnection(): Boolean {
        return when (networkMonitor.networkStatus.first()) {
            is NetworkStatusMonitor.NetworkStatus.Available -> true
            NetworkStatusMonitor.NetworkStatus.Unavailable -> false
        }
    }
}