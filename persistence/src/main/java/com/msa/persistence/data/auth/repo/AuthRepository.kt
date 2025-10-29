package com.msa.persistence.data.auth.repo

import com.msa.core.common.time.Clock
import com.msa.core.network.auth.TokenStore
import com.msa.persistence.common.device.DeviceIdProvider
import com.msa.persistence.data.auth.local.datastore.AuthSessionStore
import com.msa.persistence.data.auth.remote.AuthApi
import com.msa.persistence.domain.auth.repository.AuthRepository

fun createAuthRepository(
    api: AuthApi,
    localDataSource: AuthSessionStore,
    tokenStore: TokenStore,
    clock: Clock = Clock.System,
    deviceIdProvider: DeviceIdProvider? = null,
): AuthRepository = AuthRepositoryImpl(api, localDataSource, tokenStore, clock, deviceIdProvider)