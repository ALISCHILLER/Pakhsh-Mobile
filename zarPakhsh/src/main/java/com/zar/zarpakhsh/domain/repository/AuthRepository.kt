package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import kotlinx.coroutines.flow.Flow


interface AuthRepository {
    fun login(loginRequest: LoginRequest): Flow<NetworkResult<User>>
}