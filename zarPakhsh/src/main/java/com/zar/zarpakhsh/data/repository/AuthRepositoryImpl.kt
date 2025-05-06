package com.zar.zarpakhsh.data.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.HttpClientFactory
import com.zar.core.data.network.handler.NetworkException
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.repository.BaseRepository
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.data.mappers.toUser
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.data.remote.NetworkService
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val networkService: NetworkService, // وابسته به API
    private val localDataSourceAuth: LocalDataSourceAuth,
    private val client: HttpClientFactory// وابسته به ذخیره‌سازی محلی
) : AuthRepository, BaseRepository() {


    override suspend fun login(loginRequest: LoginRequest): NetworkResult<User> {
        return makeApiCall {
            // فرض کنید apiCall از یک API داخلی می‌آید که درخواست لاگین را ارسال می‌کند
            // این apiCall می‌تواند مربوط به یک سرویس خاص باشد که با سرور ارتباط برقرار می‌کند.
            networkService.login(loginRequest)
        }
    }




}
