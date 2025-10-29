package com.msa.persistence.data.auth.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity.Companion.PRIMARY_KEY
/**
 * Snapshot of the authenticated user persisted on disk. The entity is designed to be stored as a
 * singleton record in Room while keeping the same structure the rest of the module expects.
 */
@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey val sessionId: Int = PRIMARY_KEY,
    @Embedded(prefix = "user_") val user: UserEntity,
    @Embedded(prefix = "token_") val token: TokenEntity,
    val storedAtMillis: Long,
) {
    companion object {
        const val PRIMARY_KEY: Int = 0
    }
}



data class UserEntity(
    val id: Long,
    val username: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val roles: List<String>,
    val appFlavor: String,
    val lastLoginAt: Long?,
)

data class TokenEntity(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long?,
    val tokenType: String?,
)

fun AuthSessionEntity.withPrimaryKey(): AuthSessionEntity =
    if (sessionId == PRIMARY_KEY) this else copy(sessionId = PRIMARY_KEY)