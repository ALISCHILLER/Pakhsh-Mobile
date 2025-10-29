package com.msa.persistence.data.auth.local.datastore

import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.common.result.Meta
import com.msa.persistence.common.prefs.SessionPrefs
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthLocalDataSource(
    private val sessionPrefs: SessionPrefs,
    private val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers(),
) : AuthSessionStore {

    private val sessionState = MutableStateFlow(sessionPrefs.readSession())
    private val metaState = MutableStateFlow(sessionPrefs.readMeta())

    override val session: Flow<AuthSessionEntity?> = sessionState.asStateFlow()
    val meta: Flow<Meta?> = metaState.asStateFlow()

    override suspend fun persist(session: AuthSessionEntity, metadata: Meta?) {
        withContext(dispatchers.io) {
            sessionPrefs.writeSession(session)
            if (metadata != null) {
                sessionPrefs.writeMeta(metadata)
            } else {
                sessionPrefs.clearMeta()
            }
        }
        sessionState.value = session
        metaState.value = metadata
    }

    override suspend fun clear() {
        withContext(dispatchers.io) {
            sessionPrefs.clearSession()
        }
        sessionState.value = null
        metaState.value = null
    }

    override fun current(): AuthSessionEntity? = sessionState.value

    override fun currentMeta(): Meta? = metaState.value
}