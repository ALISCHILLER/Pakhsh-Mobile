package com.msa.zarpakhsh.domain.repository

import com.msa.zarpakhsh.domain.entities.User
import kotlinx.coroutines.flow.Flow

/**
 * واسط (Interface) لایه Domain برای عملیات‌های احراز هویت.
 * این واسط قراردادهایی را تعریف می‌کند که لایه Data باید پیاده‌سازی کند.
 * لایه UI/ViewModel تنها با این واسط سروکار دارد و از جزئیات پیاده‌سازی Data Layer بی‌خبر است.
 */
interface AuthRepository {
    /**
     * تلاش برای ورود کاربر با نام کاربری و رمز عبور.
     * نتیجه نهایی (کاربر در صورت موفقیت، یا Null در صورت شکست اولیه) را برمی‌گرداند
     * یا در صورت خطای قابل توجه، استثناء پرتاب می‌کند.
     */
    @Throws(com.msa.core.data.network.handler.NetworkException::class) // اعلام اینکه ممکن است NetworkException پرتاب شود
    suspend fun login(username: String, password: String): User?

    /**
     * عملیات خروج کاربر.
     */
    @Throws(com.msa.core.data.network.handler.NetworkException::class)
    suspend fun logout()

    /**
     * بررسی وضعیت ورود کاربر به صورت یک Flow.
     */
    fun isLoggedIn(): Flow<Boolean>
}