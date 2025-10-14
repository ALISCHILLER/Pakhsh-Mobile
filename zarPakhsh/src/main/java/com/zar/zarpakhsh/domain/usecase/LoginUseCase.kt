package com.zar.zarpakhsh.domain.usecase

import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(loginRequest: LoginRequest):  Flow<NetworkResult<User>> {
        return authRepository.login(loginRequest)
    }
}