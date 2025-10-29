package com.msa.persistence.domain.auth.model

/**
 * Domain token snapshot that mirrors what is persisted locally.
 */
data class Token(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long?,
    val tokenType: String?,
)

fun Token.isExpired(referenceEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
    val expiry = expiresAtEpochSeconds ?: return false
    return expiry <= referenceEpochSeconds
}