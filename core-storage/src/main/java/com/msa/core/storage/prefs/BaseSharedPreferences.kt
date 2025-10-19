package com.msa.core.storage.prefs

interface BaseSharedPreferences {
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
    suspend fun remove(key: String)
}