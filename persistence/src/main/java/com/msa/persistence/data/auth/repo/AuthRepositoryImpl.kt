package com.msa.persistence.data.auth.repo


import com.msa.core.common.error.AppError
import com.msa.core.common.result.Meta
import com.msa.core.common.result.Outcome
import com.msa.core.common.time.Clock
import com.msa.core.network.auth.TokenStore
import com.msa.persistence.BuildConfig
import com.msa.persistence.common.device.DeviceIdProvider
import com.msa.persistence.data.auth.dto.LoginRequest
import com.msa.persistence.data.auth.dto.LoginResponse
import com.msa.persistence.data.auth.dto.TokenDto
import com.msa.persistence.data.auth.local.datastore.AuthSessionStore
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity
import com.msa.persistence.data.auth.local.entity.TokenEntity
import com.msa.persistence.data.auth.local.entity.UserEntity
import com.msa.persistence.data.auth.remote.AuthApi
import com.msa.persistence.domain.auth.model.AuthUser
import com.msa.persistence.domain.auth.model.Token
import com.msa.persistence.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val localDataSource: AuthSessionStore,
    private val tokenStore: TokenStore,
    private val clock: Clock = Clock.System,
    private val deviceIdProvider: DeviceIdProvider? = null,
) : AuthRepository {

    private val appFlavor: String = BuildConfig.APP_FLAVOR.ifBlank { "default" }

    override val session: Flow<AuthUser?> = localDataSource.session
        .onStart { emit(localDataSource.current()) }
        .map { it?.toDomain() }

    override suspend fun login(username: String, password: String): Outcome<AuthUser> {
        val request = LoginRequest(
            username = username.trim(),
            password = password,
            appFlavor = appFlavor,
            deviceId = deviceIdProvider?.get()?.takeIf { it.isNotBlank() },
        )
        return when (val outcome = api.login(request)) {
            is Outcome.Success -> {
                val session = outcome.value.toSessionEntity(appFlavor, clock)
                if (!persistSession(session, outcome.meta)) {
                    Timber.e("Failed to persist login session for %s", username)
                    Outcome.Failure(AppError.Unknown(message = "Unable to persist authentication session"))
                } else {
                    Outcome.Success(session.toDomain(), outcome.meta)
                }
            }
            is Outcome.Failure -> {
                Timber.w(outcome.error, "Login failed for %s", username)
                outcome
            }
        }
    }

    override suspend fun logout(): Outcome<Unit> {
        if (!localDataSource.clear()) {
            Timber.w("Failed to clear persisted auth session during logout")
        }
        tokenStore.clear()
        return Outcome.Success(Unit)
    }

    override suspend fun currentUser(): AuthUser? = localDataSource.current()?.toDomain()

    override suspend fun sessionMeta(): Meta? = localDataSource.currentMeta()

    private suspend fun persistSession(session: AuthSessionEntity, meta: Meta): Boolean {
        val persisted = localDataSource.persist(session, meta)
        if (persisted) {
            tokenStore.updateTokens(session.token.accessToken, session.token.refreshToken)
        }
        return persisted
    }
}

private fun LoginResponse.toSessionEntity(appFlavor: String, clock: Clock): AuthSessionEntity {
    val tokenEntity = token.toEntity(clock)
    val userEntity = UserEntity(
        id = user.id,
        username = user.username,
        fullName = user.fullName,
        email = user.email,
        phone = user.phone,
        roles = user.roles,
        appFlavor = appFlavor,
        lastLoginAt = user.lastLoginAt,
    )
    return AuthSessionEntity(user = userEntity, token = tokenEntity, storedAtMillis = clock.nowMillis())
}

private fun TokenDto.toEntity(clock: Clock): TokenEntity {
    val nowSeconds = clock.nowMillis() / 1000
    val expiry = expiresAtEpochSeconds ?: expiresInSeconds?.let { nowSeconds + it }
    return TokenEntity(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAtEpochSeconds = expiry,
        tokenType = tokenType,
    )
}

private fun AuthSessionEntity.toDomain(): AuthUser = AuthUser(
    id = user.id,
    username = user.username,
    fullName = user.fullName,
    email = user.email,
    phone = user.phone,
    roles = user.roles,
    appFlavor = user.appFlavor,
    lastLoginAt = user.lastLoginAt,
    token = Token(
        accessToken = token.accessToken,
        refreshToken = token.refreshToken,
        expiresAtEpochSeconds = token.expiresAtEpochSeconds,
        tokenType = token.tokenType,
    ),
)