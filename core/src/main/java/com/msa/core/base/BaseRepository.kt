package com.msa.core.base

import com.msa.core.data.network.handler.NetworkHandler
import com.msa.core.data.network.handler.NetworkResult

/**
 * کلاس پایه برای Repositoryها که دسترسی به NetworkHandler را فراهم می‌کند.
 * Repositoryهای فرزند می‌توانند از توابع NetworkHandler برای انجام درخواست‌های شبکه استفاده کنند.
 */
abstract class BaseRepository(val networkHandler: NetworkHandler) {

    // توابع کمکی که NetworkHandler را Wrapper می‌کنند، اگر لازم باشد
    // یا Repositoryها مستقیماً از networkHandler.getRequest/postRequest و غیره استفاده می‌کنند.
    // با توجه به RemoteDataSourceAuth، به نظر می رسد Repository مستقیما safePostRequest ندارد
    // بلکه RemoteDataSource دارد که آن RemoteDataSource از NetworkHandler استفاده می کند.

    // مثال: اگر بخواهید توابع safeRequest را در BaseRepository داشته باشید:
    protected suspend inline fun <reified T> safeGetRequest(url: String): NetworkResult<T> {
        return networkHandler.get(url)
    }

    /**
     * تابع عمومی برای انجام درخواست POST.
     */
    protected suspend inline fun <reified T> safePostRequest(url: String, body: Any): NetworkResult<T> {
        return networkHandler.post(url, body)
    }

    /**
     * تابع عمومی برای انجام درخواست PUT.
     */
    protected suspend inline fun <reified T> safePutRequest(url: String, body: Any): NetworkResult<T> {
        return networkHandler.put(url, body)
    }

    /**
     * تابع عمومی برای انجام درخواست DELETE.
     */
    protected suspend fun safeDeleteRequest(url: String): NetworkResult<Unit> {
        return networkHandler.delete(url)
    }
}