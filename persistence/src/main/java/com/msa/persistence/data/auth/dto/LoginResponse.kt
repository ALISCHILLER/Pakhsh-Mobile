package com.msa.persistence.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val user: UserDto,
    val token: TokenDto,
)

@Serializable
data class UserDto(
    val id: Long,
    val username: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val roles: List<String> = emptyList(),
    @SerialName("last_login_at") val lastLoginAt: Long? = null,
)

@Serializable
data class TokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Long? = null,
    @SerialName("expires_at") val expiresAtEpochSeconds: Long? = null,
    @SerialName("token_type") val tokenType: String? = "Bearer",
)