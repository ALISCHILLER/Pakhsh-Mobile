package com.msa.persistence.domain.auth.model



/**
 * Domain representation of the authenticated user.
 */
data class AuthUser(
    val id: Long,
    val username: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val roles: List<String>,
    val appFlavor: String,
    val lastLoginAt: Long?,
    val token: Token,
)