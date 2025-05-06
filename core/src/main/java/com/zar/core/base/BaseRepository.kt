package com.zar.core.data.network.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BaseRepository برای مدیریت درخواست‌های شبکه به صورت عمومی
 */
open class BaseRepository {

    /**
     * انجام درخواست API با استفاده از NetworkHandler و مدیریت خطا
     */
    protected suspend fun <T> safeApiCall(
        requireConnection: Boolean = true,
        apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return NetworkHandler.safeApiCall(requireConnection, apiCall)
    }

    /**
     * یک متد عمومی برای ارسال درخواست API و گرفتن نتیجه
     */
    protected suspend fun <T> makeApiCall(
        apiCall: suspend () -> ApiResponse<T>
    ): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            safeApiCall(apiCall = apiCall)
        }
    }
}
