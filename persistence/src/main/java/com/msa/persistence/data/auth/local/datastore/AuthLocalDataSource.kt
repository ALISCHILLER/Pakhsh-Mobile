package com.msa.persistence.data.auth.local.datastore

import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.common.result.Meta
import com.msa.persistence.common.prefs.SessionPrefs
import com.msa.persistence.data.auth.local.dao.AuthSessionDao
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity
import com.msa.persistence.data.auth.local.entity.withPrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

class AuthLocalDataSource(
    private val sessionPrefs: SessionPrefs,
    private val sessionDao: AuthSessionDao,
    private val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers(),
) : AuthSessionStore {

    private val sessionState = MutableStateFlow<AuthSessionEntity?>(null)
    private val metaState = MutableStateFlow<Meta?>(null)

    init {
        val (initialSession, initialMeta) = runBlocking(dispatchers.io) {
            val storedInDb = runCatching { sessionDao.currentSession() }
                .onFailure { Timber.e(it, "Failed to query cached auth session from Room") }
                .getOrNull()

            val legacySession = if (storedInDb == null) sessionPrefs.readSession() else null
            val normalizedSession = when {
                storedInDb != null -> storedInDb
                legacySession != null -> migrateLegacySession(legacySession)
                else -> null
            }
            normalizedSession to sessionPrefs.readMeta()
        }
        sessionState.value = initialSession
        metaState.value = initialMeta
    }

    override val session: Flow<AuthSessionEntity?> = sessionState.asStateFlow()
    override val meta: Flow<Meta?> = metaState.asStateFlow()

    override suspend fun persist(session: AuthSessionEntity, metadata: Meta?): Boolean {
        val normalized = session.withPrimaryKey()
        val persisted = withContext(dispatchers.io) {
            runCatching {
                sessionDao.upsert(normalized)
                val metaPersisted = metadata?.let(sessionPrefs::writeMeta) ?: sessionPrefs.clearMeta()
                if (!metaPersisted) {
                    Timber.w("Persisted session in Room but failed to store metadata; rolling back session")
                    sessionDao.clear()
                    false
                } else {
                    if (!sessionPrefs.clearLegacySession()) {
                        Timber.w("Stored session in Room but could not clear legacy shared preferences copy")
                    }
                    true
                }
            }.onFailure { Timber.e(it, "Failed to persist auth session into Room") }
                .getOrDefault(false)
        }
        if (persisted) {
            sessionState.value = normalized
            metaState.value = metadata
        }
        return persisted
    }


    override suspend fun clear(): Boolean {
        val cleared = withContext(dispatchers.io) {
            runCatching {
                sessionDao.clear()
                sessionPrefs.clearSession()
            }.onFailure { Timber.e(it, "Failed to clear cached auth session") }
                .getOrDefault(false)
        }
        if (cleared) {
            sessionState.value = null
            metaState.value = null
        }
        return cleared
    }

    override fun current(): AuthSessionEntity? = sessionState.value

    override fun currentMeta(): Meta? = metaState.value

    private suspend fun migrateLegacySession(legacy: AuthSessionEntity): AuthSessionEntity? {
        val normalized = legacy.withPrimaryKey()
        return runCatching {
            sessionDao.upsert(normalized)
            if (!sessionPrefs.clearLegacySession()) {
                Timber.w("Legacy session migrated to Room but could not clear shared preferences copy")
            }
            normalized
        }.onFailure { Timber.e(it, "Failed to migrate legacy auth session from shared preferences") }
            .getOrNull() ?: legacy
    }
}