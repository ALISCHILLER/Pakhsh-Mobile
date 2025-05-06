package com.zar.zarpakhsh.data.remote

import com.zar.core.data.network.model.ApiResponse
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse

interface NetworkService {

    /**
     * ارسال درخواست لاگین
     */
    suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse>
}