package com.msa.persistence.domain.auth.repository

import com.msa.core.common.result.Meta
import com.msa.core.common.result.Outcome
import com.msa.persistence.domain.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val session: Flow<AuthUser?>

    suspend fun login(username: String, password: String): Outcome<AuthUser>

    suspend fun logout(): Outcome<Unit>

    suspend fun currentUser(): AuthUser?

    suspend fun sessionMeta(): Meta?
}