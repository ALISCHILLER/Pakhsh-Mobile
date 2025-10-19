package com.msa.core.storage.api

import kotlinx.coroutines.flow.Flow

interface SecureKeyValue {
    suspend fun putString(key: String, value: String)
    suspend fun putLong(key: String, value: Long)
    suspend fun getString(key: String): String?
    suspend fun getLong(key: String): Long?
    suspend fun remove(key: String)
    suspend fun clear()
}

interface ObservableKeyValue : SecureKeyValue {
    fun observeString(key: String): Flow<String?>
}

interface TokenStore : SecureKeyValue {
    suspend fun writeTokens(accessToken: String, refreshToken: String, expiresAtEpochSeconds: Long)
    suspend fun readTokens(): TokenSnapshot?
    suspend fun clearTokens()
}

data class TokenSnapshot(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long
)