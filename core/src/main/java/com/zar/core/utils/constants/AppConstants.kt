package com.zar.core.utils.constants


/**
 * ثابت‌های سبک و بدون حساسیت برای کلیدها و نام‌های مشترک.
 */
object AppConstants {

    // SharedPreferences
    const val SHARED_PREF_NAME = "msa_app_preferences"
    const val USER_PREFERENCES = "user_preferences"
    const val PREF_IS_USER_LOGGED_IN = "is_user_logged_in"
    const val PREF_USER_SESSION_KEY = "user_session_key"
    const val PREF_IS_FIRST_TIME_USER = "is_first_time_user"
    const val PREF_IS_USER_VERIFIED = "is_user_verified"

    // Theme identifiers
    const val THEME_LIGHT = "light_theme"
    const val THEME_DARK = "dark_theme"
    const val DEFAULT_THEME = THEME_LIGHT

    // Analytics keys
    const val ANALYTICS_USER_ID = "user_id"
    const val ANALYTICS_EVENT_LOGIN = "login_event"
    const val ANALYTICS_EVENT_SIGNUP = "signup_event"

    // Locale defaults
    const val DEFAULT_LANGUAGE = "fa"
    const val DEFAULT_COUNTRY_CODE = "IR"

    // File formats
    const val FILE_FORMAT_PNG = "image/png"
    const val FILE_FORMAT_JPEG = "image/jpeg"
    const val FILE_FORMAT_PDF = "application/pdf"
    const val FILE_FORMAT_TXT = "text/plain"
}
