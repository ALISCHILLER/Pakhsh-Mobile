package com.msa.core.data.network.handler

/**
 * یک sealed class برای مدیریت وضعیت‌های مختلف عملیات‌های غیرهمزمان.
 */
sealed class NetworkResult<out T> {

    /**
     * وضعیت موفقیت‌آمیز: شامل داده‌ها که از API یا منبع داده دیگر برگشته است.
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()

    /**
     * وضعیت خطا: شامل پیغام خطا یا استثناء که در زمان انجام عملیات رخ داده است.
     */
    data class Error(val exception: Throwable? = null, val message: String? = null) : NetworkResult<Nothing>()

    /**
     * وضعیت در حال بارگذاری: برای نشان دادن اینکه عملیات در حال اجراست.
     */
    object Loading : NetworkResult<Nothing>()
}