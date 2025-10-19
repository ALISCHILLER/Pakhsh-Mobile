package com.msa.core.storage.token

import com.msa.core.network.auth.TokenStore
import com.msa.core.storage.prefs.BaseSharedPreferences

class TokenStoreEncrypted(
    private val preferences: BaseSharedPreferences
) : TokenStore {

    private companion object {
        const val KEY_ACCESS = "token_access"
        const val KEY_REFRESH = "token_refresh"
    }

    override suspend fun accessToken(): String? = preferences.getString(KEY_ACCESS)

    override suspend fun refreshToken(): String? = preferences.getString(KEY_REFRESH)

    override suspend fun updateTokens(access: String, refresh: String?) {
        preferences.putString(KEY_ACCESS, access)
        if (refresh != null) {
            preferences.putString(KEY_REFRESH, refresh)
        } else {
            preferences.remove(KEY_REFRESH)
        }
    }

    override suspend fun clear() {
        preferences.remove(KEY_ACCESS)
        preferences.remove(KEY_REFRESH)
    }
}