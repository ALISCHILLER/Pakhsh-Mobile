package com.msa.core.network.status

interface NetworkStatusMonitor {
    fun isOnline(): Boolean
}