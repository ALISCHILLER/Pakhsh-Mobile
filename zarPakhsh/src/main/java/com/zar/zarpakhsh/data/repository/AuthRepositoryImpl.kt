package com.zar.zarpakhsh.data.repository

import com.zar.core.base.BaseRepository
import com.zar.core.data.network.result.NetworkResult
import com.zar.core.data.network.result.map
import com.zar.zarpakhsh.data.mappers.toUser
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    networkHandler: NetworkHandler
) : AuthRepository, BaseRepository(networkHandler) {


    override fun login(loginRequest: LoginRequest): Flow<NetworkResult<User>> {
        return postAsFlow<LoginResponse>(
            url = ApiEndpoints.LOGIN,
            body = loginRequest
        ).map { result ->
            result.map { response ->
                response.toUser()
            }
        }
    }
}