package com.zar.zarpakhsh.data.models

/**
 * مدل داده‌ای برای پاسخ API لاگین.
 */
data class LoginResponse(
    val userId: String,
    val username: String,
    val email: String,
    val token: String
)