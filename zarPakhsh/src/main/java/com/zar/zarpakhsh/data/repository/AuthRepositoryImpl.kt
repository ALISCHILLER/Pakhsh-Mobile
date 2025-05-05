package com.zar.zarpakhsh.data.repository

import android.content.Context
import com.zar.core.base.BaseRepository
import com.zar.core.data.network.handler.NetworkException
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.handler.NetworkResult
import com.zar.core.data.network.model.NetworkConfig
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.data.mappers.toUser
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    networkHandler: NetworkHandler,
    private val localDataSourceAuth: LocalDataSourceAuth // وابسته به ذخیره‌سازی محلی
) : AuthRepository, BaseRepository(networkHandler) {

    override   suspend fun login(username: String, password: String): NetworkResult<User> {
        val loginRequest = LoginRequest(username, password)
        return safePostRequest(ApiEndpoints.LOGIN.toString(), loginRequest)
    }
}
