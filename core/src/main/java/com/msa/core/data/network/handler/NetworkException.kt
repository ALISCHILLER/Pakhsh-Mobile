package com.msa.core.data.network.handler

/**
 * استثناء‌ای برای مدیریت خطاهای خاص شبکه.
 */
class NetworkException(
    val errorCode: Int, // کد خطای HTTP یا کد خاص
    val errorMessage: String, // پیام خطای مرتبط
    override val cause: Throwable? = null, // دلیل اصلی استثناء (در صورت وجود)
    val retryCount: Int = 0 // تعداد تلاش‌های مجدد (اختیاری)
) : Exception(errorMessage, cause) {

    /**
     * سازنده جایگزین برای استفاده از Enum برای کدهای خطا.
     */
    constructor(
        errorCode: NetworkErrorCode,
        errorMessage: String,
        cause: Throwable? = null,
        retryCount: Int = 0
    ) : this(errorCode.code, errorMessage, cause, retryCount)

    /**
     * بازنویسی متد toString برای لاگ‌گیری جزئیات خطا.
     */
    override fun toString(): String {
        return "NetworkException(errorCode=$errorCode, errorMessage='$errorMessage', retryCount=$retryCount)"
    }

    /**
     * Enum برای مدیریت کدهای خطا به‌صورت ساختارمند.
     */
    enum class NetworkErrorCode(val code: Int, val description: String) {
        NOT_FOUND(404, "Resource not found"),
        SERVER_ERROR(500, "Internal server error"),
        TIMEOUT(408, "Request timeout"),
        UNAUTHORIZED(401, "Unauthorized access"),
        FORBIDDEN(403, "Access forbidden"),
        UNKNOWN(-1, "Unknown error")
    }
}