package com.zar.core.data.network.common

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.error.*
import com.zar.core.data.network.result.NetworkResult
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object NetworkHandler {

    lateinit var appContext: Context
        private set

    lateinit var networkStatusMonitor: NetworkStatusMonitor
        private set

    lateinit var client: HttpClient
        private set

    @Volatile
    private var initialized = false
    fun isInitialized(): Boolean = initialized

    private fun ensureInitialized() {
        check(initialized) { "NetworkHandler not initialized" }
    }

    fun initialize(context: Context, monitor: NetworkStatusMonitor, httpClient: HttpClient) {
        if (initialized) return
        appContext = context.applicationContext
        networkStatusMonitor = monitor
        client = httpClient
        initialized = true
    }

    fun hasNetworkConnection(): Boolean {
        ensureInitialized()
        return networkStatusMonitor.currentStatus() is NetworkStatusMonitor.NetworkStatus.Available
    }
    fun <T> handleApiResponse(response: ApiResponse<T>): NetworkResult<T> {
        ensureInitialized()
        Timber.d("Handling API response: $response")
        return if (!response.hasError) {
            val data = response.data
            if (data != null) {
                NetworkResult.Success(data)
            } else {
                NetworkResult.Error(
                    ParsingError(
                        errorCode = "empty_data",
                        message = appContext.getString(R.string.error_server_generic)
                    )
                )
            }
        } else {
            Timber.e("API response error: ${response.message}, code=${response.code}")
            NetworkResult.Error(
                ApiError(
                    errorCode = response.code.toString(),
                    message = response.message.ifBlank { appContext.getString(R.string.error_server) },
                    statusCode = response.code
                )
            )
        }
    }

    suspend inline fun <reified T> safeApiCall(
        requireConnection: Boolean = true,
        noinline apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return try {
            if (requireConnection && !hasNetworkConnection()) {
                Timber.e("No network connection available")
                NetworkResult.Error(
                    ConnectionError(
                        errorCode = "network_unavailable",
                        message = appContext.getString(R.string.error_no_connection),
                        connectionType = null
                    )
                )
            } else {
                withContext(Dispatchers.IO) { handleApiResponse(apiCall()) }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Timber.e(e, "API call failed")
            NetworkResult.Error.fromException(e, appContext)
        }
    }

    // ---------- HTTP verbs (typed over ApiResponse<T>) ----------

    suspend inline fun <reified T> get(url: String): NetworkResult<T> {
        Timber.d("GET request to $url")
        val result = safeApiCall { client.get(url).body<ApiResponse<T>>() }
        Timber.d("GET result from $url: $result")
        return result
    }


    suspend inline fun <reified Req, reified Res> post(url: String, body: Req): NetworkResult<Res> {
        Timber.d("POST request to $url with body: $body")
        val result = safeApiCall { client.post(url) { setBody(body) }.body<ApiResponse<Res>>() }
        Timber.d("POST result from $url: $result")
        return result
    }
    suspend inline fun <reified Req, reified Res> put(url: String, body: Req): NetworkResult<Res> {
        Timber.d("PUT request to $url with body: $body")
        val result = safeApiCall { client.put(url) { setBody(body) }.body<ApiResponse<Res>>() }
        Timber.d("PUT result from $url: $result")
        return result
    }
    suspend inline fun <reified Req, reified Res> patch(url: String, body: Req): NetworkResult<Res> {
        Timber.d("PATCH request to $url with body: $body")
        val result = safeApiCall { client.patch(url) { setBody(body) }.body<ApiResponse<Res>>() }
        Timber.d("PATCH result from $url: $result")
        return result
    }
    suspend inline fun <reified T> delete(url: String): NetworkResult<T> {
        Timber.d("DELETE request to $url")
        val result = safeApiCall { client.delete(url).body<ApiResponse<T>>() }
        Timber.d("DELETE result from $url: $result")
        return result
    }
    // HEAD → Unit (no body parsing)
    suspend fun head(url: String): NetworkResult<Unit> {
        Timber.d("HEAD request to $url")
        val result = safeApiCall {
            val resp = client.head(url)
            ApiResponse(
                code = resp.status.value,
                status = resp.status.description,
                data = Unit,
                message = resp.status.description,
                hasError = !resp.status.isSuccess()
            )
        }
        Timber.d("HEAD result from $url: $result")
        return result
    }
}
