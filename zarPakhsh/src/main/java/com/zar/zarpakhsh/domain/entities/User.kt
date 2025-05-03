package com.zar.zarpakhsh.domain.entities

/**
 * موجودیت کاربر برای لاگین.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val token: String  // توکن احراز هویت
)