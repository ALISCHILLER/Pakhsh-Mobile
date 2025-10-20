package com.msa.core.storage.impl

import com.msa.core.storage.api.ObservableKeyValue
import com.msa.core.storage.api.TokenSnapshot
import com.msa.core.storage.api.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class InMemorySecureStorage : ObservableKeyValue, TokenStore {
    private val mutex = Mutex() // برای متدهای suspend
    private val stateLock = ReentrantLock() // برای متدهای غیر-suspend
    private val data = mutableMapOf<String, String>()
    private val observers = mutableMapOf<String, MutableStateFlow<String?>>()

    override suspend fun putString(key: String, value: String) {
        mutex.withLock {
            data[key] = value
            observers[key]?.value = value
        }
    }

    override suspend fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }

    override suspend fun getString(key: String): String? = mutex.withLock { data[key] }

    override suspend fun getLong(key: String): Long? = getString(key)?.toLongOrNull()

    override suspend fun remove(key: String) {
        mutex.withLock {
            data.remove(key)
            observers[key]?.value = null
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            data.clear()
            observers.values.forEach { it.value = null }
        }
    }

    override fun observeString(key: String): Flow<String?> {
        // اینجا دیگه از Mutex استفاده نمی‌کنیم چون تابع suspend نیست
        val flow = stateLock.withLock {
            observers.getOrPut(key) { MutableStateFlow(data[key]) }
        }
        return flow.asStateFlow()
    }

    override suspend fun writeTokens(accessToken: String, refreshToken: String, expiresAtEpochSeconds: Long) {
        putString(KEY_ACCESS, accessToken)
        putString(KEY_REFRESH, refreshToken)
        putLong(KEY_EXPIRES, expiresAtEpochSeconds)
    }

    override suspend fun readTokens(): TokenSnapshot? {
        val access = getString(KEY_ACCESS)
        val refresh = getString(KEY_REFRESH)
        val expires = getLong(KEY_EXPIRES)
        return if (access != null && refresh != null && expires != null) {
            TokenSnapshot(access, refresh, expires)
        } else null
    }

    override suspend fun clearTokens() {
        remove(KEY_ACCESS)
        remove(KEY_REFRESH)
        remove(KEY_EXPIRES)
    }

    companion object {
        private const val KEY_ACCESS = "auth.access"
        private const val KEY_REFRESH = "auth.refresh"
        private const val KEY_EXPIRES = "auth.expiresAt"
    }
}