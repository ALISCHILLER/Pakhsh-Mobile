package com.msa.supervisorapp.config

import com.msa.core.common.config.AppConfig
import com.msa.supervisorApp.BuildConfig

object SupervisorAppConfig : AppConfig {
    override val flavorName: String
        get() = BuildConfig.FLAVOR

    override val baseUrl: String
        get() = BuildConfig.BASE_URL

    override val dbName: String
        get() = "supervisor.db"

    override val sharedPreferencesName: String
        get() = "supervisor_prefs"

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