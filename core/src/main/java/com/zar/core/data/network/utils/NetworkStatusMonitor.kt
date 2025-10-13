package com.zar.core.data.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.jvm.Volatile

class NetworkStatusMonitor(
    context: Context,
    private val httpClient: HttpClient
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Volatile
    private var lastStatus: NetworkStatus = determineCurrentStatus()

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Network available")
                val status = determineCurrentStatus()
                lastStatus = status
                trySend(status)
            }

            override fun onLost(network: Network) {
                Timber.w("Network lost")
                val status = NetworkStatus.Unavailable
                lastStatus = status
                trySend(status)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Timber.d("Network capabilities changed")
                val status = determineCurrentStatus()
                lastStatus = status
                trySend(status)
            }

            override fun onUnavailable() {
                Timber.e("Network unavailable")
                val status = NetworkStatus.Unavailable
                lastStatus = status
                trySend(status)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                }
            }
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        val initialStatus = determineCurrentStatus()
        lastStatus = initialStatus
        trySend(initialStatus)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .onEach { lastStatus = it }
        .conflate()
        .distinctUntilChanged()

    val isOnline: Flow<Boolean> = networkStatus.map { it is NetworkStatus.Available }

    fun refreshStatus(): NetworkStatus {
        val refreshed = determineCurrentStatus()
        lastStatus = refreshed
        return refreshed
    }

    fun hasNetworkConnection(): Boolean = lastStatus is NetworkStatus.Available

    fun currentConnectionType(): ConnectionType? =
        (lastStatus as? NetworkStatus.Available)?.connectionType

    fun lastKnownStatus(): NetworkStatus = lastStatus

    fun peekStatus(): NetworkStatus = lastStatus

    sealed class NetworkStatus {
        data class Available(val connectionType: ConnectionType) : NetworkStatus()
        object Unavailable : NetworkStatus()
    }

    enum class ConnectionType {
        WIFI,
        CELLULAR,
        ETHERNET,
        UNKNOWN;

        fun isMetered(): Boolean = this == CELLULAR
    }

    private fun determineCurrentStatus(): NetworkStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkStatus.Unavailable
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return NetworkStatus.Unavailable

            when {
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> {
                    val type = when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                            ConnectionType.WIFI
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                            ConnectionType.CELLULAR
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                            ConnectionType.ETHERNET
                        else -> ConnectionType.UNKNOWN
                    }
                    NetworkStatus.Available(type)
                }
                else -> NetworkStatus.Unavailable
            }
        } else {
            @Suppress("DEPRECATION")
            val info = connectivityManager.activeNetworkInfo
            if (info?.isConnected == true) {
                NetworkStatus.Available(
                    when (info.type) {
                        ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                        ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                        ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                        else -> ConnectionType.UNKNOWN
                    }
                )
            } else {
                NetworkStatus.Unavailable
            }
        }
    }
    suspend fun isBackendReachable(path: String, query: Map<String, String?> = emptyMap()): Boolean {
        return try {
            val response = httpClient.get(path) {
                query.forEach { (key, value) ->
                    value?.let { parameter(key, it) }
                }
            }
            response.status == HttpStatusCode.OK
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Backend reachability check failed")
            false
        }
    }
}