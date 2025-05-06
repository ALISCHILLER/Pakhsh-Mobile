package com.zar.zarpakhsh.domain.usecase

import android.content.Context
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(
    private val authRepository: AuthRepository
) {

    suspend fun execute(loginRequest: LoginRequest): NetworkResult<LoginResponse> {
        return authRepository.login(loginRequest)
    }
}