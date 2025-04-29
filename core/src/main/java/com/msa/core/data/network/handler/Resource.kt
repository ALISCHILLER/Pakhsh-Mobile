package com.msa.core.data.network.handler

/**
 * یک sealed class برای مدیریت وضعیت‌های مختلف عملیات‌های غیرهمزمان.
 */
sealed class Resource<out T> {

    /**
     * وضعیت موفقیت‌آمیز: شامل داده‌ها که از API یا منبع داده دیگر برگشته است.
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * وضعیت خطا: شامل پیغام خطا یا استثناء که در زمان انجام عملیات رخ داده است.
     */
    data class Error(val exception: Throwable? = null, val message: String? = null) : Resource<Nothing>()

    /**
     * وضعیت در حال بارگذاری: برای نشان دادن اینکه عملیات در حال اجراست.
     */
    object Loading : Resource<Nothing>()
}