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

    override suspend fun accessToken(): String? = preferences.getStringOrNull(KEY_ACCESS)

    override suspend fun refreshToken(): String? = preferences.getStringOrNull(KEY_REFRESH)

    override suspend fun updateTokens(access: String, refresh: String?) {
        preferences.saveString(KEY_ACCESS, access, commit = true)
        if (refresh != null) {
            preferences.saveString(KEY_REFRESH, refresh, commit = true)
        } else {
            preferences.removeKey(KEY_REFRESH, commit = true)
        }
    }

    override suspend fun clear() {
        preferences.removeKeys(KEY_ACCESS, KEY_REFRESH, commit = true)
    }
}