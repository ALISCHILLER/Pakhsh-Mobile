package com.msa.core.data.network.model



/**
 * مدل پاسخ API.
 * این کلاس نمایانگر ساختار کلی داده‌های دریافتی از API است.
 */
data class ApiResponse<T>(
    val code: Int? = null, // کد پاسخ HTTP یا کد سفارشی API
    val status: String, // وضعیت پاسخ (مانند "success" یا "error")
    val data: T? = null, // داده‌های اصلی بازگشتی از API
    val message: String? = null, // پیام مرتبط با پاسخ (برای خطا یا موفقیت)
    val hasError: Boolean // یک فیلد بولین برای نشان دادن وقوع خطای منطقی در سمت سرور
)