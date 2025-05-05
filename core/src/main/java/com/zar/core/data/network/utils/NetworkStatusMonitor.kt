package com.zar.core.data.network.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber

class NetworkStatusMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Network available")
                trySend(getCurrentNetworkStatus())
            }

            override fun onLost(network: Network) {
                Timber.w("Network lost")
                trySend(NetworkStatus.Unavailable)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Timber.d("Network capabilities changed")
                trySend(getCurrentNetworkStatus())
            }

            override fun onUnavailable() {
                Timber.e("Network unavailable")
                trySend(NetworkStatus.Unavailable)
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
        trySend(getCurrentNetworkStatus())

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    val isOnline: Flow<Boolean> = networkStatus.map { it is NetworkStatus.Available }

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

    fun getCurrentNetworkStatus(): NetworkStatus {
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
}