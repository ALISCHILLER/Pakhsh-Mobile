package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkException
import com.zar.zarpakhsh.data.models.LoginRequest
import kotlinx.coroutines.flow.Flow


interface AuthRepository {
    fun login(loginRequest: LoginRequest): Flow<NetworkResult<User>>
}