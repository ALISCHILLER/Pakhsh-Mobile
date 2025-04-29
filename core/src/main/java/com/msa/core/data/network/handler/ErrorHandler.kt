package com.msa.core.data.network.handler

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber // فرض می‌شود کتابخانه Timber برای لاگ‌گیری استفاده می‌شود

/**
 * کلاس Singleton برای مدیریت خطاها و تبدیل استثناء‌ها به پیغام‌های قابل فهم برای کاربر یا لاگ‌گیری.
 * هدف این کلاس مرکزی‌سازی منطق مدیریت خطا در لایه Data/Network است.
 */
object ErrorHandler {

    /**
     * تبدیل استثناء به یک پیام خطای مشخص برای نمایش به کاربر.
     * این تابع Exception واقعی را دریافت کرده و یک String محلی شده برمی‌گرداند.
     *
     * @param exception استثناء رخ داده شده (غیر Null)
     * @return پیغام خطا مناسب برای کاربر
     */
    fun handleError(exception: Throwable): String {
        // این تابع Exception غیر Null را می پذیرد
        val errorMessage = when (exception) {
            is IOException -> {
                "خطا در اتصال به اینترنت. لطفاً وضعیت اتصال خود را بررسی کنید."
            }
            is TimeoutCancellationException -> {
                "زمان درخواست تمام شده است. لطفاً دوباره امتحان کنید."
            }
            is CancellationException -> {
                // لغو شدن عملیات معمولاً نباید به عنوان یک خطا به کاربر نمایش داده شود،
                // اما ممکن است نیاز به لاگ‌گیری داشته باشد.
                "عملیات لغو شده است."
            }
            is NetworkException -> {
                // اگر از NetworkException سفارشی استفاده می کنید، می توانید پیام آن را مستقیماً استفاده کنید.
                // یا بر اساس errorCode پیام دقیق‌تری نمایش دهید.
                exception.errorMessage
            }
            else -> {
                // پیام استثناء اصلی یا یک پیام عمومی در صورت ناشناخته بودن
                exception.localizedMessage ?: exception.message ?: "خطای ناشناخته رخ داده است."
            }
        }

        // لاگ‌گیری جزئیات خطا برای Debugging
        // ارسال exception به Timber باعث نمایش Stack Trace می شود.
        Timber.e(exception, "An error occurred: $errorMessage")

        return errorMessage
    }

    /**
     * پردازش و مدیریت خطاهای شبکه دریافتی از safeApiCall.
     * این تابع یک Throwable? را می‌گیرد و یک NetworkResult.Error ایجاد می‌کند که
     * حاوی یک Throwable غیر Null و یک پیام است.
     *
     * @param exception استثناء رخ داده شده (ممکن است Null باشد در موارد خاص)
     * @return شیء NetworkResult.Error حاوی یک Throwable غیر Null و پیغام خطا
     */
    fun handleNetworkError(exception: Throwable?): NetworkResult.Error {
        // اطمینان از اینکه Exception داخلی NetworkResult.Error هرگز Null نباشد
        val finalException = exception ?: Throwable("Unknown error occurred during network operation.")

        // لاگ‌گیری اولیه خطا قبل از تبدیل آن به NetworkResult.Error
        Timber.e(finalException, "Handling network error")

        // استفاده از handleError برای تولید پیام کاربری (اختیاری - NetworkResult.Error فقط پیام خام را هم می تواند نگه دارد)
        // در این پیاده سازی ساده، message همان پیام exception است.
        val message = finalException.localizedMessage ?: finalException.message ?: "خطای ناشناخته"

        return NetworkResult.Error(finalException, message)
    }

    /**
     * تابعی کمکی برای ایجاد NetworkResult.Error از روی یک پیام و Exception اختیاری.
     * مفید زمانی که خطایی غیر از Exception از API دریافت می شود (مثلا ApiResponse با hasError = true).
     */
    fun createErrorResult(message: String?, exception: Throwable? = null): NetworkResult.Error {
        val finalException = exception ?: Throwable(message ?: "Specific error occurred")
        val finalMessage = message ?: finalException.localizedMessage ?: finalException.message ?: "Unknown specific error"
        Timber.e(finalException, "Creating specific error result: $finalMessage")
        return NetworkResult.Error(finalException, finalMessage)
    }
}