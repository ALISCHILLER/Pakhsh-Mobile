package com.msa.core.utils.constants

import androidx.compose.ui.unit.dp

/**
 * ثابت‌های مربوط به شبکه
 */
object NetworkConstants {

    // URL های پایه و مسیرهای API
    const val BASE_URL = "https://api.example.com/" // URL پایه برای درخواست‌های شبکه
    const val IMAGE_BASE_URL = "https://images.example.com/" // URL برای تصاویر
    const val API_VERSION = "v1" // نسخه API

    // مسیرهای مختلف API
    const val LOGIN_URL = "auth/login"
    const val REGISTER_URL = "auth/register"
    const val GET_USER_PROFILE_URL = "user/profile"
    const val UPDATE_USER_PROFILE_URL = "user/update"
    const val FETCH_NOTIFICATIONS_URL = "notifications/fetch"

    // پارامترهای عمومی درخواست‌های API
    const val AUTHORIZATION_HEADER = "Authorization" // هدر برای تایید هویت
    const val CONTENT_TYPE_HEADER = "Content-Type" // هدر برای نوع محتوا
    const val ACCEPT_HEADER = "Accept" // هدر برای پذیرش نوع محتوا
    const val APPLICATION_JSON = "application/json" // نوع محتوا JSON

    // زمان‌سنجی‌ها
    const val CONNECT_TIMEOUT = 15L // زمان‌سنجی اتصال به سرور (بر حسب ثانیه)
    const val READ_TIMEOUT = 15L // زمان‌سنجی خواندن داده‌ها از سرور (بر حسب ثانیه)
    const val WRITE_TIMEOUT = 15L // زمان‌سنجی نوشتن داده‌ها به سرور (بر حسب ثانیه)

    // کدهای وضعیت HTTP
    const val HTTP_OK = 200 // درخواست موفقیت‌آمیز
    const val HTTP_CREATED = 201 // داده جدید با موفقیت ایجاد شده است
    const val HTTP_BAD_REQUEST = 400 // درخواست نادرست
    const val HTTP_UNAUTHORIZED = 401 // کاربر تایید هویت نشده است
    const val HTTP_FORBIDDEN = 403 // دسترسی غیرمجاز
    const val HTTP_NOT_FOUND = 404 // داده یافت نشد
    const val HTTP_INTERNAL_SERVER_ERROR = 500 // خطای داخلی سرور

    // پیام‌های خطای شبکه
    const val NETWORK_ERROR = "خطای شبکه، لطفا اتصال اینترنت خود را بررسی کنید."
    const val SERVER_ERROR = "خطای سرور، لطفا دوباره تلاش کنید."
    const val TIMEOUT_ERROR = "اتصال به سرور قطع شد، لطفا دوباره تلاش کنید."

    // پارامترهای عمومی دیگر
    const val DEFAULT_PAGE_SIZE = 20 // تعداد آیتم‌های پیش‌فرض در هر صفحه
    const val MAX_PAGE_SIZE = 100 // حداکثر تعداد آیتم‌ها در یک صفحه

    // پیام‌ها برای نمایش خطای عمومی
    const val GENERAL_ERROR_MESSAGE = "عملیات با خطا مواجه شد، لطفا دوباره تلاش کنید."
    const val INVALID_CREDENTIALS_MESSAGE = "نام کاربری یا رمز عبور اشتباه است."
    const val UNEXPECTED_ERROR_MESSAGE = "خطای غیرمنتظره رخ داده است."

    // زمان‌های پیش‌فرض
    val DEFAULT_LOADING_TIMEOUT = 10 * 1000L // زمان بارگذاری پیش‌فرض (۱۰ ثانیه)

    // اندازه‌های پیش‌فرض (برای تنظیمات UI)
    val LOADING_INDICATOR_SIZE = 48.dp // اندازه پیش‌فرض نشان‌گر بارگذاری

}
