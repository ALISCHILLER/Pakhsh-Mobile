package com.zar.zarpakhsh.data.remote

import com.zar.core.base.BaseRepository
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse

class RemoteDataSourceAuth(
    networkHandler: NetworkHandler
) : BaseRepository(networkHandler) {

    /**
     * انجام درخواست لاگین به API.
     * نتیجه عملیات را در NetworkResult کپسوله کرده و برمی‌گرداند.
     */
    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse> {
        // فراخوانی safePostRequest که از NetworkHandler استفاده میکند
        return safePostRequest(ApiEndpoints.LOGIN, request)
    }

    /**
     * انجام درخواست لاگ اوت به API.
     */
    suspend fun logout(): NetworkResult<Unit> {
        // پاسخ لاگ اوت معمولاً داده خاصی ندارد (Unit)
        return safePostRequest(ApiEndpoints.LOGOUT, emptyMap<String, Any>()) // ارسال بدنه خالی یا Map خالی
    }
}