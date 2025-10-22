package com.msa.core.data.network.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class NetworkStatusMonitor(context: Context) : Closeable {



private val appContext = context.applicationContext
private val connectivityManager =
    appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

private val closed = AtomicBoolean(false)
private val callback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        Timber.d("Network available: %s", network)
        refresh()
    }

    override fun onLost(network: Network) {
        Timber.w("Network lost: %s", network)
        updateStatus(NetworkStatus.Unavailable)
    }

    override fun onUnavailable() {
        Timber.e("Network unavailable")
        updateStatus(NetworkStatus.Unavailable)
    }

    override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
        Timber.d("Network capabilities changed: %s", capabilities)
        refresh()
    }
}
private val _status = MutableStateFlow(determineStatus())
val networkStatus: StateFlow<NetworkStatus> = _status.asStateFlow()

private val _isOnline = MutableStateFlow(_status.value is NetworkStatus.Available)
val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
init {
    registerCallback()
}

fun currentStatus(): NetworkStatus = _status.value

fun refresh() {
    updateStatus(determineStatus())
}


@Synchronized
override fun close() {
    if (closed.compareAndSet(false, true)) {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
            .onFailure { Timber.w(it, "Failed to unregister network callback") }
    }
}

// Backward-compat alias
fun getCurrentNetworkStatus(): NetworkStatus = currentStatus()

private fun registerCallback() {
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

    val registered = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
                .onFailure {
                    Timber.w(
                        it,
                        "Default network callback registration failed; falling back to request-based listener"
                    )
                }
                .isSuccess
        }

        else -> false
    }

    if (!registered) {
        runCatching { connectivityManager.registerNetworkCallback(request, callback) }
            .onFailure { Timber.w(it, "Network request callback registration failed") }
    }
    refresh()
}

private fun updateStatus(status: NetworkStatus) {
    if (_status.value == status) return
    _status.value = status
    _isOnline.value = status is NetworkStatus.Available
}

private fun determineStatus(): NetworkStatus {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.Unavailable
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkStatus.Unavailable
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
        connectivityManager.activeNetworkInfo?.let { info ->
            if (info.isConnected) {
                val type = when (info.type) {
                    ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                    ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                    else -> ConnectionType.UNKNOWN
                }
                NetworkStatus.Available(type)
            } else {
                NetworkStatus.Unavailable
            }
        } ?: NetworkStatus.Unavailable
    }
}

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
}