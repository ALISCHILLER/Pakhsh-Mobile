package com.msa.core.utils.validation


import java.util.concurrent.atomic.AtomicReference
/**
 * اعتبارسنجی شماره موبایل ایران.
 *
 * امکانات:
 * - پشتیبانی از ورودی با اعداد فارسی/عربی و حذف جداکننده‌ها/فاصله‌ها
 * - پذیرش قالب‌های دارای کد کشور (+98 / 0098 / 98) و تبدیل به فرم داخلی 11 رقمی (09xxxxxxxxx)
 * - بررسی طول، الگوی کلی، و پیش‌شماره‌های معتبر
 *  * - ارایه نتیجه‌ی ساخت‌یافته (موفق/ناموفق) همراه با فرمت بین‌المللی E164
 *  * - قابلیت بازنشانی پیش‌شماره‌ها به مقدار پیش‌فرض
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
    fun resetPrefixesToDefault() {
        prefixStore.set(defaultPrefixes)
    }

    fun currentPrefixes(): Set<String> = prefixStore.get()


    // الگوی شماره داخلی استاندارد ایران: 11 رقم، شروع با 09
    private val iranLocalRegex = Regex("^09\\d{9}$")


    sealed class MobileValidationResult {
        data class Success(val normalized: String) : MobileValidationResult() {
            val international: String get() = "+98" + normalized.removePrefix("0")
        }

        data class Failure(val error: Error) : MobileValidationResult() {
            enum class Error(val message: String) {
                EMPTY_INPUT("شماره موبایل نباید خالی باشد."),
                INVALID_LENGTH("طول شماره موبایل باید ۱۱ رقم باشد (فرم صحیح: 09xxxxxxxxx)."),
                MISSING_LEADING_ZERO("شماره موبایل باید با ۰ شروع شود."),
                INVALID_PREFIX("پیش‌شماره نامعتبر است."),
                INVALID_FORMAT("قالب شماره موبایل نامعتبر است.")
            }
        }

        val isSuccess: Boolean get() = this is Success
    }

    fun validate(mobileNumber: String?): MobileValidationResult {
        if (mobileNumber.isNullOrBlank()) {
            return MobileValidationResult.Failure(MobileValidationResult.Failure.Error.EMPTY_INPUT)
        }

        val trimmed = mobileNumber.trim()
        val digitsOnly = mapToWesternDigits(mobileNumber).filter { it.isDigit() }
        val normalized = normalizeToLocal(mobileNumber)

        if (normalized == null) {
            val error = when {
                digitsOnly.isEmpty() -> MobileValidationResult.Failure.Error.INVALID_FORMAT
                digitsOnly.length != 11 && !trimmed.startsWith("+98") &&
                        !digitsOnly.startsWith("0098") && !digitsOnly.startsWith("98") ->
                    MobileValidationResult.Failure.Error.INVALID_LENGTH
                digitsOnly.firstOrNull()?.let { it != '0' } == true &&
                        !trimmed.startsWith("+98") && !digitsOnly.startsWith("0098") && !digitsOnly.startsWith("98") ->
                    MobileValidationResult.Failure.Error.MISSING_LEADING_ZERO
                else -> MobileValidationResult.Failure.Error.INVALID_FORMAT
            }
            return MobileValidationResult.Failure(error)
        }

        if (normalized.length != 11) {
            return MobileValidationResult.Failure(MobileValidationResult.Failure.Error.INVALID_LENGTH)
        }
        if (!iranLocalRegex.matches(normalized)) {
            return MobileValidationResult.Failure(MobileValidationResult.Failure.Error.INVALID_FORMAT)
        }
        if (normalized.take(4) !in currentPrefixes()) {
            return MobileValidationResult.Failure(MobileValidationResult.Failure.Error.INVALID_PREFIX)
        }

        return MobileValidationResult.Success(normalized)
    }



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
    fun isValid(mobileNumber: String): Boolean = validate(mobileNumber).isSuccess

    fun formatToE164(mobileNumber: String): String? =
        when (val result = validate(mobileNumber)) {
            is MobileValidationResult.Success -> result.international
            is MobileValidationResult.Failure -> null
        }

    /**
     * اعتبارسنجی با برگرداندن پیام خطا (null یعنی معتبر).
     */
    fun validateAndGetErrorMessage(mobileNumber: String): String? {
        return when (val result = validate(mobileNumber)) {
            is MobileValidationResult.Success -> null
            is MobileValidationResult.Failure -> result.error.message
        }
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
