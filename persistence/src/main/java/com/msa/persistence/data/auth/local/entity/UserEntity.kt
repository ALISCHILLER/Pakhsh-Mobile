package com.msa.persistence.data.auth.local.entity

import androidx.room.Entity


@Entity("user")
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

data class AuthSessionEntity(
    val user: UserEntity,
    val token: TokenEntity,
    val storedAtMillis: Long,
)