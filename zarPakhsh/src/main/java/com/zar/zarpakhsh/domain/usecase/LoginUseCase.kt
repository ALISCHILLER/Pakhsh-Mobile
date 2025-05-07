package com.zar.zarpakhsh.domain.usecase

import android.content.Context
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun execute(loginRequest: LoginRequest):  Flow<NetworkResult<User>> {
        return authRepository.login(loginRequest)
    }
}