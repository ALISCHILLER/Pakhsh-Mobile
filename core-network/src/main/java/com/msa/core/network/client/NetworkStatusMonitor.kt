package com.msa.core.network.client

import kotlinx.coroutines.flow.Flow

/**
 * Represents the current connectivity state of the device. Implementations are expected to
 * surface a hot [Flow] that emits immediately with the latest connectivity information and keep
 * it up to date as the network changes.
 */
interface NetworkStatusMonitor {
    val isConnected: Flow<Boolean>
    fun isOnline(): Boolean
}