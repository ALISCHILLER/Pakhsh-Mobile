package com.msa.core.utils.validation


import java.util.concurrent.atomic.AtomicReference
/**
 * اعتبارسنجی شماره موبایل ایران.
 *
 * امکانات:
 * - پشتیبانی از ورودی با اعداد فارسی/عربی و حذف جداکننده‌ها/فاصله‌ها
 * - پذیرش قالب‌های دارای کد کشور (+98 / 0098 / 98) و تبدیل به فرم داخلی 11 رقمی (09xxxxxxxxx)
 * - بررسی طول، الگوی کلی، و پیش‌شماره‌های معتبر
 */
object MobileNumberValidator {

    private val defaultPrefixes: Set<String> = setOf(
        "0912", "0919",
        "0930", "0933", "0934", "0935", "0936", "0937", "0938", "0939",
        "0901", "0902", "0903", "0904", "0905",
        "0920", "0921", "0922",
        "0990"
    )

    private val prefixStore = AtomicReference(defaultPrefixes)

    fun configureValidPrefixes(prefixes: Collection<String>) {
        val sanitized = prefixes.mapNotNull { it.trim().takeIf(String::isNotEmpty) }.toSet()
        if (sanitized.isNotEmpty()) {
            prefixStore.set(sanitized)
        }
    }

    fun currentPrefixes(): Set<String> = prefixStore.get()


    // الگوی شماره داخلی استاندارد ایران: 11 رقم، شروع با 09
    private val iranLocalRegex = Regex("^09\\d{9}$")

    /**
     * تلاش برای نرمال‌سازی ورودی به قالب داخلی (09xxxxxxxxx).
     * @return رشته‌ی نرمال‌شده 11 رقمی یا null اگر نادرست باشد.
     */
    fun normalizeToLocal(input: String?): String? {
        if (input.isNullOrBlank()) return null

        // 1) تبدیل اعداد فارسی/عربی به انگلیسی + حذف هرچیز غیرعددی
        val digits = mapToWesternDigits(input).filter { it.isDigit() }

        // 2) حذف پیشوند کد کشور ایران
        val normalized = when {
            digits.startsWith("0098") && digits.length >= 13 -> "0" + digits.substring(4) // 0098XXXXXXXXX...
            digits.startsWith("98")   && digits.length >= 12 -> "0" + digits.substring(2) // 98XXXXXXXXX...
            digits.startsWith("0")    && digits.length == 11 -> digits                     // 09xxxxxxxxx
            // حالت +98...
            input.trim().startsWith("+98") && digits.length >= 12 -> "0" + digits.substring(digits.length - 10)
            else -> digits
        }

        // نهایتاً باید 11 رقمی و با 09 شروع شود
        return if (iranLocalRegex.matches(normalized)) normalized else null
    }

    /**
     * چک ساده‌ی اعتبار: true/false.
     */
    fun isValid(mobileNumber: String): Boolean {
        val local = normalizeToLocal(mobileNumber) ?: return false
        return local.take(4) in currentPrefixes()
    }

    /**
     * اعتبارسنجی با برگرداندن پیام خطا (null یعنی معتبر).
     */
    fun validateAndGetErrorMessage(mobileNumber: String): String? {
        if (mobileNumber.isBlank()) return "شماره موبایل نباید خالی باشد."

        val local = normalizeToLocal(mobileNumber)
            ?: return when {
                // تلاش برای تشخیص سریع‌تر خطا
                mapToWesternDigits(mobileNumber).filter { it.isDigit() }.length != 11 ->
                    "طول شماره موبایل باید ۱۱ رقم باشد (یا با کد کشور ایران وارد نکنید)."
                !mapToWesternDigits(mobileNumber).contains('0') ->
                    "شماره موبایل باید با ۰ شروع شود."
                else -> "قالب شماره موبایل نامعتبر است."
            }

        if (local.length != 11) return "طول شماره موبایل باید ۱۱ رقم باشد."
        if (!iranLocalRegex.matches(local)) return "قالب شماره موبایل نامعتبر است."
        if (local.take(4) !in currentPrefixes()) return "پیش‌شماره نامعتبر است."

        return null
    }

    // ---------- ابزارهای داخلی ----------

    // نگاشت ارقام فارسی/عربی به انگلیسی
    private fun mapToWesternDigits(s: String): String {
        val sb = StringBuilder(s.length)
        s.forEach { ch ->
            sb.append(
                when (ch) {
                    // فارسی
                    '۰' -> '0'; '۱' -> '1'; '۲' -> '2'; '۳' -> '3'; '۴' -> '4'
                    '۵' -> '5'; '۶' -> '6'; '۷' -> '7'; '۸' -> '8'; '۹' -> '9'
                    // عربی
                    '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                    '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }
}
