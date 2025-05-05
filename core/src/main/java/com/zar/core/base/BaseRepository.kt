package com.zar.core.data.network.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * کلاس پایه برای تمام Repositoryهای شبکه‌ای که:
 * - مدیریت خطا را فراهم می‌کند
 * - تمام متدهای API را به صورت safe فراخوانی می‌کند
 */
abstract class BaseRepository(
    val networkHandler: NetworkHandler
) {

    // ================================
    // HTTP Methods with Safe Wrapping
    // ================================

    /**
     * درخواست GET ایمن با مدیریت خطا و وضعیت‌های شبکه
     */
    protected suspend inline fun <reified T> safeGet(
        url: String,
        step: String = "GET:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.get(url).body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }

    /**
     * درخواست POST ایمن با بدنی و مدیریت خطا
     */
    protected suspend inline fun <reified T> safePost(
        url: String,
        crossinline body: () -> Any,
        step: String = "POST:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.post(url) {
                setBody(body())
            }.body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }

    /**
     * درخواست PUT ایمن با بدنی و مدیریت خطا
     */
    protected suspend inline fun <reified T> safePut(
        url: String,
        crossinline body: () -> Any,
        step: String = "PUT:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.put(url) {
                setBody(body())
            }.body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }

    /**
     * درخواست PATCH ایمن با بدنی و مدیریت خطا
     */
    protected suspend inline fun <reified T> safePatch(
        url: String,
        crossinline body: () -> Any,
        step: String = "PATCH:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.patch(url) {
                setBody(body())
            }.body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }

    /**
     * درخواست DELETE ایمن با مدیریت خطا
     */
    protected suspend inline fun <reified T> safeDelete(
        url: String,
        step: String = "DELETE:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.delete(url).body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }

    /**
     * درخواست HEAD ایمن با مدیریت خطا
     */
    protected suspend inline fun <reified T> safeHead(
        url: String,
        step: String = "HEAD:$url",
        requireConnection: Boolean = true
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        networkHandler.safeApiCall(requireConnection = requireConnection) {
            ApiResponse(data = networkHandler.client.value.head(url).body(), hasError = false)
        }.also { result ->
            if (result is NetworkResult.Error) {
                Timber.e("API Error in $step: ${result.message} (Code: ${result.httpCode})")
            }
        }
    }
}