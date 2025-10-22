package com.msa.core.flags

import com.msa.core.common.coroutines.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RemoteFeatureFlags(
    private val remoteConfig: RemoteConfig,
    private val dispatchers: CoroutineDispatchers,
    private val cacheTtl: Duration = 5.minutes,
) : FeatureFlags {

    private val trackedBooleans = ConcurrentHashMap<String, Boolean>()
    private val trackedStrings = ConcurrentHashMap<String, String>()
    private val trackedLongs = ConcurrentHashMap<String, Long>()

    private val lastRefreshMillis = AtomicLong(0L)
    private val _snapshot = MutableStateFlow(FlagSnapshot())
    val snapshot = _snapshot.asStateFlow()

    suspend fun refresh(force: Boolean = false): Boolean = dispatchers.withIo {
        val now = System.currentTimeMillis()
        if (!force && now - lastRefreshMillis.get() < cacheTtl.inWholeMilliseconds) {
            return@withIo false
        }

        val activated = remoteConfig.fetchAndActivate()
        val booleans = trackedBooleans.mapValues { (key, default) ->
            runCatching { remoteConfig.getBoolean(key, default) }.getOrDefault(default)
        }
        val strings = trackedStrings.mapValues { (key, default) ->
            runCatching { remoteConfig.getString(key, default) }.getOrDefault(default)
        }
        val longs = trackedLongs.mapValues { (key, default) ->
            runCatching { remoteConfig.getLong(key, default) }.getOrDefault(default)
        }
        _snapshot.value = FlagSnapshot(booleans, strings, longs)
        lastRefreshMillis.set(now)
        activated
    }

    override fun isEnabled(key: String, default: Boolean): Boolean {
        return _snapshot.value.booleans[key] ?: remoteConfig.getBoolean(key, default)
    }

    override fun getVariant(key: String, default: String?): String? {
        val cached = _snapshot.value.strings[key]
        return when {
            cached != null -> cached
            default != null -> remoteConfig.getString(key, default)
            else -> remoteConfig.getString(key)
        }
    }

    fun observeBoolean(key: String, default: Boolean = false): Flow<Boolean> {
        registerBoolean(key, default)
        return snapshot.map { it.booleans[key] ?: default }.distinctUntilChanged()
    }

    fun observeString(key: String, default: String = ""): Flow<String> {
        registerString(key, default)
        return snapshot.map { it.strings[key] ?: default }.distinctUntilChanged()
    }

    fun observeLong(key: String, default: Long = 0L): Flow<Long> {
        registerLong(key, default)
        return snapshot.map { it.longs[key] ?: default }.distinctUntilChanged()
    }

    private fun registerBoolean(key: String, default: Boolean) {
        _snapshot.update { snapshot ->
            if (snapshot.booleans.containsKey(key)) snapshot else snapshot.copy(
                booleans = snapshot.booleans + (key to default)
            )
        }
        trackedBooleans[key] = default
    }

    private fun registerString(key: String, default: String) {
        _snapshot.update { snapshot ->
            if (snapshot.strings.containsKey(key)) snapshot else snapshot.copy(
                strings = snapshot.strings + (key to default)
            )
        }
        trackedStrings[key] = default
    }

    private fun registerLong(key: String, default: Long) {
        _snapshot.update { snapshot ->
            if (snapshot.longs.containsKey(key)) snapshot else snapshot.copy(
                longs = snapshot.longs + (key to default)
            )
        }
        trackedLongs[key] = default
    }

    private data class FlagSnapshot(
        val booleans: Map<String, Boolean> = emptyMap(),
        val strings: Map<String, String> = emptyMap(),
        val longs: Map<String, Long> = emptyMap(),
    )
}
