package com.zar.zarpakhsh.utils.config

import com.zar.core.config.AppConfig
import com.zar.zarpakhsh.BuildConfig


object AppConfigZar : AppConfig {
    override val appFlavor: String
        get() = BuildConfig.APP_FLAVOR

    override val sharedPreferencesName: String
        get() = when (appFlavor) {
            "visit" -> "visit_prefs"
            "supervisor" -> "supervisor_prefs"
            "distribute" -> "distribute_prefs"
            else -> "default_prefs"
        }

    override val databaseName: String
        get() = when (appFlavor) {
            "visit" -> "visit_database"
            "supervisor" -> "supervisor_database"
            "distribute" -> "distribute_database"
            else -> "default_database"
        }
    override val signalRUrl: String
        get() = when (appFlavor) {
            "visit" -> "https://signalr.visit.zar.com/hub"
            "supervisor" -> "https://signalr.supervisor.zar.com/hub"
            "distribute" -> "https://signalr.distribute.zar.com/hub"
            else -> "https://signalr.zar.com/hub"
        }
}