package com.msa.core.data.network.utils





import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber


class NetworkStatusMonitor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Network available")
                trySend(currentStatus())
            }
            override fun onLost(network: Network) {
                Timber.w("Network lost")
                trySend(NetworkStatus.Unavailable)
            }
            override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) {
                Timber.d("Network capabilities changed")
                trySend(currentStatus())
            }
            override fun onUnavailable() {
                Timber.e("Network unavailable")
                trySend(NetworkStatus.Unavailable)
            }
        }


        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET) }
            .build()


        cm.registerNetworkCallback(req, cb)
        trySend(currentStatus())
        awaitClose { cm.unregisterNetworkCallback(cb) }
    }.distinctUntilChanged()


    val isOnline: Flow<Boolean> = networkStatus.map { it is NetworkStatus.Available }


    sealed class NetworkStatus {
        data class Available(val connectionType: ConnectionType) : NetworkStatus()
        object Unavailable : NetworkStatus()
    }


    enum class ConnectionType { WIFI, CELLULAR, ETHERNET, UNKNOWN; fun isMetered() = this == CELLULAR }


    fun currentStatus(): NetworkStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork ?: return NetworkStatus.Unavailable
            val caps = cm.getNetworkCapabilities(n) ?: return NetworkStatus.Unavailable
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return NetworkStatus.Unavailable
            val type = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.UNKNOWN
            }
            NetworkStatus.Available(type)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.let { info ->
                if (info.isConnected) {
                    val type = when (info.type) {
                        ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                        ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                        ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                        else -> ConnectionType.UNKNOWN
                    }
                    NetworkStatus.Available(type)
                } else NetworkStatus.Unavailable
            } ?: NetworkStatus.Unavailable
        }
    }


    // Backward-compat alias
    fun getCurrentNetworkStatus(): NetworkStatus = currentStatus()
}