package com.msa.core.network.auth

interface TokenStore {
    suspend fun accessToken(): String?
    suspend fun refreshToken(): String?
    suspend fun updateTokens(access: String, refresh: String?)
    suspend fun clear()
}