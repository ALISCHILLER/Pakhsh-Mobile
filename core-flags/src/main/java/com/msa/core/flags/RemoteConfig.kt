package com.msa.core.flags

interface RemoteConfig {
    suspend fun fetchAndActivate(): Boolean
    fun getString(key: String, default: String = ""): String
    fun getLong(key: String, default: Long = 0): Long
    fun getBoolean(key: String, default: Boolean = false): Boolean
}