package com.msa.core.utils.constants

import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

/**
 * ثابت‌های عمومی پروژه که در سراسر اپلیکیشن استفاده می‌شود.
 */
object AppConstants {

    // اطلاعات اپلیکیشن
    const val APP_NAME = "MSA App" // نام اپلیکیشن
    const val APP_VERSION = "1.0.0" // نسخه اپلیکیشن
    const val MINIMUM_API_LEVEL = 21 // حداقل نسخه اندروید پشتیبانی‌شده

    // ثابت‌های مربوط به زمان‌ها
    const val SPLASH_SCREEN_DURATION = 2000L // مدت زمان نمایش صفحه آغازین (در میلی‌ثانیه)
    const val SESSION_TIMEOUT_DURATION = 30L // زمان تایم‌اوت جلسه (دقیقه)
    const val DEFAULT_CACHE_EXPIRATION = 1L // زمان انقضای کش (روز)

    // ثابت‌های مربوط به وضعیت‌های اپلیکیشن
    const val IS_USER_LOGGED_IN = "is_user_logged_in" // کلید برای بررسی ورود کاربر
    const val USER_SESSION_KEY = "user_session_key" // کلید برای ذخیره سشن کاربر
    const val IS_FIRST_TIME_USER = "is_first_time_user" // کلید برای بررسی ورود اولین بار کاربر
    const val IS_USER_VERIFIED = "is_user_verified" // کلید برای بررسی تایید هویت کاربر

    // ثابت‌های مربوط به SharedPreferences
    const val SHARED_PREF_NAME = "msa_app_preferences" // نام SharedPreferences
    const val USER_PREFERENCES = "user_preferences" // SharedPreferences مربوط به کاربر

    // ثابت‌های مربوط به نوردهی (Theme)
    const val LIGHT_THEME = "light_theme" // حالت تم روشن
    const val DARK_THEME = "dark_theme" // حالت تم تاریک
    const val DEFAULT_THEME = LIGHT_THEME // تم پیش‌فرض

    // اندازه‌ها و فواصل UI
    val BUTTON_CORNER_RADIUS = 12.dp // شعاع گوشه دکمه‌ها
    val DEFAULT_PADDING = 16.dp // padding پیش‌فرض
    val DEFAULT_MARGIN = 16.dp // margin پیش‌فرض
    val ICON_SIZE = 24.dp // اندازه آیکون‌ها
    val BUTTON_HEIGHT = 48.dp // ارتفاع پیش‌فرض دکمه‌ها

    // ثابت‌های مربوط به خطاها و پیام‌ها
    const val ERROR_UNKNOWN = "خطای ناشناخته، لطفا دوباره تلاش کنید."
    const val ERROR_NETWORK = "خطای شبکه، لطفا اتصال اینترنت خود را بررسی کنید."
    const val ERROR_SERVER = "مشکلی در سرور پیش آمده است."
    const val ERROR_NO_DATA = "داده‌ای یافت نشد."
    const val ERROR_INVALID_INPUT = "ورودی نامعتبر است."
    const val ERROR_UNAUTHORIZED = "دسترسی غیرمجاز. لطفا وارد حساب کاربری خود شوید."

    // ثابت‌های مربوط به تاریخ و زمان
    const val DATE_FORMAT = "yyyy-MM-dd" // فرمت تاریخ
    const val TIME_FORMAT = "HH:mm:ss" // فرمت زمان
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss" // فرمت تاریخ و زمان
    const val DATE_TIMEZONE = "UTC" // منطقه زمانی پیش‌فرض

    // URL های مرتبط با اپلیکیشن
    const val PRIVACY_POLICY_URL = "https://www.example.com/privacy-policy" // URL سیاست حفظ حریم خصوصی
    const val TERMS_AND_CONDITIONS_URL = "https://www.example.com/terms-and-conditions" // URL شرایط استفاده

    // کلیدهای مرتبط با تحلیل داده
    const val ANALYTICS_USER_ID = "user_id" // شناسه کاربر برای تجزیه و تحلیل
    const val ANALYTICS_EVENT_LOGIN = "login_event" // رویداد ورود
    const val ANALYTICS_EVENT_SIGNUP = "signup_event" // رویداد ثبت‌نام

    // کدهای وضعیت (Status Codes)
    const val SUCCESS = 200 // درخواست موفقیت‌آمیز
    const val ERROR = 500 // خطای داخلی سرور

    // فرمت‌های فایل
    const val FILE_FORMAT_PNG = "image/png" // فرمت تصویر PNG
    const val FILE_FORMAT_JPEG = "image/jpeg" // فرمت تصویر JPEG
    const val FILE_FORMAT_PDF = "application/pdf" // فرمت فایل PDF
    const val FILE_FORMAT_TXT = "text/plain" // فرمت فایل TXT

    // زمان‌های پیش‌فرض (برای تایم‌اوت‌ها و تاخیرها)
    val TIMEOUT_DURATION = 15L // مدت زمان پیش‌فرض تایم‌اوت (در ثانیه)
    val RETRY_INTERVAL = 5000L // مدت زمان پیش‌فرض بین تلاش‌های مجدد (در میلی‌ثانیه)
    val CACHE_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(1) // زمان انقضای کش (۱ ساعت)

    // ویژگی‌های امنیتی
    const val ENCRYPTION_KEY = "some_secure_encryption_key" // کلید رمزنگاری پیش‌فرض
    const val API_KEY = "your_api_key_here" // کلید API برای دسترسی به سرویس‌های خارجی

    // متغیرهای پیش‌فرض برای API
    const val DEFAULT_LANGUAGE = "fa" // زبان پیش‌فرض فارسی
    const val DEFAULT_COUNTRY_CODE = "IR" // کد کشور پیش‌فرض ایران

    // نوع فونت‌ها
    const val FONT_REGULAR = "fonts/regular.ttf" // فونت معمولی
    const val FONT_BOLD = "fonts/bold.ttf" // فونت ضخیم
    const val FONT_ITALIC = "fonts/italic.ttf" // فونت کج

    // سایر ثابت‌ها
    const val MAX_FILE_UPLOAD_SIZE = 10 * 1024 * 1024 // حداکثر حجم آپلود فایل (۱۰ مگابایت)
    const val MAX_RETRY_ATTEMPTS = 3 // حداکثر تعداد تلاش‌های مجدد
}
val appName = AppConstants.APP_NAME

//// استفاده از URLهای سیاست حفظ حریم خصوصی
//val privacyPolicyUrl = AppConstants.PRIVACY_POLICY_URL
//
//// استفاده از زمان تایم‌اوت
//val timeoutDuration = AppConstants.TIMEOUT_DURATION
//
//// استفاده از کلید API
//val apiKey = AppConstants.API_KEY
//
//// نمایش پیام خطا در صورت بروز مشکل
//if (networkError) {
//    Toast.makeText(context, AppConstants.ERROR_NETWORK, Toast.LENGTH_SHORT).show()
//}