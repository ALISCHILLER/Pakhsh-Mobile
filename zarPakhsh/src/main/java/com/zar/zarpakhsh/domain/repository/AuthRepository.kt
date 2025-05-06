package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkException
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.domain.entities.User
import kotlinx.coroutines.flow.Flow


interface AuthRepository {

    @Throws(NetworkException::class) // اعلام اینکه ممکن است NetworkException پرتاب شود
    suspend fun login(loginRequest: LoginRequest): NetworkResult<User>


}