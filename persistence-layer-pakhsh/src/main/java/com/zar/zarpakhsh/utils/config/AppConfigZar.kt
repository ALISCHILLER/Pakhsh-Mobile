package com.zar.persistenc.utils.config

import com.zar.core.config.AppConfig
import com.zar.persistenc.BuildConfig

enum class AppRole { VISIT, SUPERVISOR, DISTRIBUTE, DEFAULT }
enum class AppEnv  { DEV, STAGE, PROD }

/** اگر در gradle فیلدهای ROLE/ENV داری، این نگاشت‌ها ساده‌تر می‌شوند. */
private fun resolveRole(): AppRole = when ((BuildConfig.ROLE_FLAVOR ?: BuildConfig.FLAVOR).lowercase()) {
    "visit"      -> AppRole.VISIT
    "supervisor" -> AppRole.SUPERVISOR
    "distribute" -> AppRole.DISTRIBUTE
    else         -> AppRole.DEFAULT
}

private fun resolveEnv(): AppEnv = when ((BuildConfig.ENV_FLAVOR ?: "prod").lowercase()) {
    "dev"   -> AppEnv.DEV
    "stage" -> AppEnv.STAGE
    else    -> AppEnv.PROD
}

object AppConfigZar : AppConfig {
    private val role = resolveRole()
    private val env  = resolveEnv()

    override val appFlavor: String
        get() = "${role.name.lowercase()}-${env.name.lowercase()}"

    override val sharedPreferencesName: String
        get() = when (role) {
            AppRole.VISIT      -> "visit_prefs"
            AppRole.SUPERVISOR -> "supervisor_prefs"
            AppRole.DISTRIBUTE -> "distribute_prefs"
            AppRole.DEFAULT    -> "default_prefs"
        }

    override val databaseName: String
        get() = when (role) {
            AppRole.VISIT      -> "visit.db"
            AppRole.SUPERVISOR -> "supervisor.db"
            AppRole.DISTRIBUTE -> "distribute.db"
            AppRole.DEFAULT    -> "default.db"
        }

    /** پیشنهاد: baseUrl عمومی برای APIهای REST */
    val baseUrl: String = when (env) {
        AppEnv.DEV   -> "https://dev.api.zar.com"
        AppEnv.STAGE -> "https://stage.api.zar.com"
        AppEnv.PROD  -> "https://api.zar.com"
    }

    override val signalRUrl: String
        get() = when (role) {
            AppRole.VISIT      -> "${signalRBase()}/visit/hub"
            AppRole.SUPERVISOR -> "${signalRBase()}/supervisor/hub"
            AppRole.DISTRIBUTE -> "${signalRBase()}/distribute/hub"
            AppRole.DEFAULT    -> "${signalRBase()}/hub"
        }

    private fun signalRBase(): String = when (env) {
        AppEnv.DEV   -> "https://signalr.dev.zar.com"
        AppEnv.STAGE -> "https://signalr.stage.zar.com"
        AppEnv.PROD  -> "https://signalr.zar.com"
    }
}
