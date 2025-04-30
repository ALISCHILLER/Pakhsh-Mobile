package com.msa.core.data.network.model

/**
 * مدل استاندارد پاسخ API که شامل اطلاعات وضعیت، داده‌ها و خطاهای منطقی است.
 */
data class ApiResponse<T>(
    val code: Int? = null, // کد HTTP یا کد سفارشی API
    val status: String, // وضعیت پاسخ (success, error, ...)
    val data: T? = null, // داده‌های موفقیت‌آمیز
    val message: String? = null, // پیام خطا یا موفقیت
    val hasError: Boolean // نشانگر خطای منطقی سرور
)