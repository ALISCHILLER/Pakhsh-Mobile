package com.msa.zarpakhsh.domain.exceptions

/**
 * خطاهای مرتبط با احراز هویت.
 */
sealed class AuthException(message: String) : Exception(message) {
    object InvalidCredentials : AuthException("نام کاربری یا رمز عبور نادرست است.")
    object NetworkError : AuthException("خطای شبکه رخ داده است.")
    object ServerError : AuthException("خطای سرور رخ داده است.")
}