package com.msa.persistence.domain.auth.usecase

import com.msa.core.common.error.AppError
import com.msa.core.common.result.Outcome
import com.msa.persistence.domain.auth.model.AuthUser
import com.msa.persistence.domain.auth.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(username: String, password: String): Outcome<AuthUser> {
        val sanitizedUsername = username.trim()
        val sanitizedPassword = password.trim()
        if (sanitizedUsername.isEmpty() || sanitizedPassword.isEmpty()) {
            return Outcome.Failure(
                AppError.Business(
                    message = "Username or password is empty",
                    payload = mapOf("username" to sanitizedUsername, "password" to "***"),
                    businessCode = "validation_error"
                )
            )
        }
        return repository.login(sanitizedUsername, sanitizedPassword)
    }
}