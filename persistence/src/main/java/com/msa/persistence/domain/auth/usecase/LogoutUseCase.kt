package com.msa.persistence.domain.auth.usecase


import com.msa.core.common.result.Outcome
import com.msa.persistence.domain.auth.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Outcome<Unit> = repository.logout()
}