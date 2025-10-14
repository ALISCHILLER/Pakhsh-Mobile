package com.zar.core.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/** A pair of tokens returned by your backend after refresh/login. */
data class TokenPair(val accessToken: String, val refreshToken: String? = null)


/** Storage abstraction for access/refresh tokens. Provide your own (DataStore, EncryptedPrefs, ...). */
interface TokenStore {
    fun accessToken(): String?
    fun refreshToken(): String?
    suspend fun updateTokens(pair: TokenPair)
    suspend fun clear()
}

/** Simple in-memory store (good for tests/samples). Use a persistent version in production. */
class InMemoryTokenStore : TokenStore {
    private val _access = MutableStateFlow<String?>(null)
    private val _refresh = MutableStateFlow<String?>(null)


    val accessFlow: StateFlow<String?> = _access
    val refreshFlow: StateFlow<String?> = _refresh


    override fun accessToken(): String? = _access.value
    override fun refreshToken(): String? = _refresh.value
    override suspend fun updateTokens(pair: TokenPair) {
        _access.value = pair.accessToken
        _refresh.value = pair.refreshToken
    }
    override suspend fun clear() { _access.value = null; _refresh.value = null }
}
/**
 * AuthConfig wires automatic 401→refresh→retry flow into HttpClient.
 * - Provide how to refresh (refresh lambda). Factory will call it under a Mutex to avoid stampede.
 */
data class AuthConfig(
    val tokenStore: TokenStore,
    val refresh: suspend (oldAccess: String?, oldRefresh: String?) -> TokenPair?
)