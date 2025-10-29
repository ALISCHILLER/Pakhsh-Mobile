package com.msa.persistence.common.device

import android.content.Context
import android.provider.Settings
import timber.log.Timber

class AndroidDeviceIdProvider(
    private val context: Context,
) : DeviceIdProvider {

    override fun get(): String? = runCatching {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }.onFailure { error ->
        Timber.w(error, "Failed to obtain ANDROID_ID")
    }.getOrNull()
}