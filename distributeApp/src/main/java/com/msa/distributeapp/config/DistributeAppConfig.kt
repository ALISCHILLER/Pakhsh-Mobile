package com.msa.distributeapp.config

import com.msa.core.common.config.AppConfig
import com.msa.distributeapp.BuildConfig

object distributeappConfig : AppConfig {
    override val flavorName: String
        get() = BuildConfig.FLAVOR

    override val baseUrl: String
        get() = BuildConfig.BASE_URL

    override val dbName: String
        get() = "distribute.db"

    override val sharedPreferencesName: String
        get() = "distribute_prefs"

    override val enableLogging: Boolean
        get() = BuildConfig.DEBUG

    override val appVersion: String
        get() = BuildConfig.VERSION_NAME

    override val allowCleartextTraffic: Boolean
        get() = BuildConfig.DEBUG

    override val sslPinningEnabled: Boolean
        get() = !BuildConfig.DEBUG

    override val signalRUrl: String
        get() = BuildConfig.SIGNALR_URL
}