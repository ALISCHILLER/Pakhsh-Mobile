package com.msa.core.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.msa.core.network.client.NetworkStatusMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class AndroidNetworkStatusMonitor(context: Context) : Closeable, NetworkStatusMonitor {

    sealed class Status {
        data object Unavailable : Status()
        data class Available(val type: ConnectionType) : Status()
    }

    enum class ConnectionType { WIFI, CELLULAR, ETHERNET, UNKNOWN }

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
            updateStatus(Status.Unavailable)
        }

        override fun onUnavailable() {
            Timber.e("Network unavailable")
            updateStatus(Status.Unavailable)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            Timber.d("Network capabilities changed: %s", capabilities)
            refresh()
        }
    }

    private val _status = MutableStateFlow(determineStatus())
    val status: StateFlow<Status> = _status.asStateFlow()

    private val _isConnected = MutableStateFlow(_status.value is Status.Available)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        registerCallback()
    }

    override fun isOnline(): Boolean = _isConnected.value

    private fun refresh() {
        if (closed.get()) return
        updateStatus(determineStatus())
    }

    @Synchronized
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
                .onFailure { Timber.w(it, "Failed to unregister network callback") }
        }
    }

    private fun registerCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val registered = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
                .onFailure { Timber.w(it, "Default callback registration failed; falling back") }
                .isSuccess
        } else {
            false
        }

        if (!registered) {
            runCatching { connectivityManager.registerNetworkCallback(request, callback) }
                .onFailure { Timber.w(it, "Network request callback registration failed") }
        }
        refresh()
    }

    private fun updateStatus(status: Status) {
        if (_status.value == status) return
        _status.value = status
        _isConnected.value = status is Status.Available
    }

    private fun determineStatus(): Status = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val active = connectivityManager.activeNetwork ?: return Status.Unavailable
            val caps = connectivityManager.getNetworkCapabilities(active) ?: return Status.Unavailable
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return Status.Unavailable
            }
            val type = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.UNKNOWN
            }
            Status.Available(type)
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
                    Status.Available(type)
                } else {
                    Status.Unavailable
                }
            } ?: Status.Unavailable
        }
    }.getOrElse {
        Timber.w(it, "Failed to determine network availability")
        Status.Unavailable
    }
}