package com.msa.persistence.data.auth.local.datastore

import com.msa.core.common.result.Meta
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Contract for persisting the authenticated session snapshot locally. It mirrors the behaviour of
 * [AuthLocalDataSource] but is extracted so tests can supply lightweight fakes without touching
 * Android storage primitives.
 */
interface AuthSessionStore {
    val session: Flow<AuthSessionEntity?>

    suspend fun persist(session: AuthSessionEntity, metadata: Meta?)

    suspend fun clear()

    fun current(): AuthSessionEntity?

    fun currentMeta(): Meta?
}