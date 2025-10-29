package com.msa.persistence.common.device

/**
 * Provides a stable identifier for the current device to send alongside login requests.
 */
fun interface DeviceIdProvider {
    fun get(): String?
}