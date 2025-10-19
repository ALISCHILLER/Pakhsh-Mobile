package com.msa.core.common.config

/**
 * Abstraction describing environment specific configuration that must be provided
 * by application level modules so that the shared core modules can operate without
 * referencing BuildConfig directly.
 */
interface AppConfig {
    val flavorName: String
    val baseUrl: String
    val imageBaseUrl: String get() = baseUrl
    val dbName: String
    val sharedPreferencesName: String
    val enableLogging: Boolean
    val appVersion: String
    val localeTag: String? get() = null
    val allowCleartextTraffic: Boolean get() = false
    val sslPinningEnabled: Boolean get() = false
}