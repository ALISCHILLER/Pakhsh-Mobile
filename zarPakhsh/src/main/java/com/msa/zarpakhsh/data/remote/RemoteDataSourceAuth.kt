package com.msa.zarpakhsh.data.remote

import com.msa.core.base.BaseRepository
import com.msa.core.data.network.handler.NetworkHandler
import com.msa.core.data.network.handler.NetworkResult
import com.msa.core.data.network.model.ApiResponse
import com.msa.zarpakhsh.data.models.LoginRequest
import com.msa.zarpakhsh.data.models.LoginResponse

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