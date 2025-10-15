package com.zar.core.utils.validation

/**
 * اعتبارسنجی کد ملی ایران با نرمال‌سازی ورودی (ارقام فارسی/عربی، جداکننده‌ها).
 */
object NationalCodeValidator {

    /**
     * بررسی صحت کد ملی (true یعنی معتبر).
     */
    fun isValid(nationalCode: String): Boolean {
        val code = normalize(nationalCode) ?: return false

        // رد الگوهای تکراری: 0000000000، 1111111111، ...
        if (code.toSet().size == 1) return false

        val digits = code.map { it.digitToInt() }
        val checkDigit = digits.last()
        val sum = digits.take(9).foldIndexed(0) { idx, acc, d ->
            acc + d * (10 - idx)
        }
        val remainder = sum % 11
        val expected = if (remainder < 2) remainder else 11 - remainder
        return checkDigit == expected
    }

    /**
     * اعتبارسنجی با پیام خطا (null یعنی معتبر).
     */
    fun validateAndGetErrorMessage(nationalCode: String): String? {
        val raw = nationalCode
        if (raw.isBlank()) return "کد ملی نباید خالی باشد."

        val code = normalize(raw) ?: run {
            // سعی می‌کنیم علت را دقیق‌تر بگوییم
            val onlyDigits = mapToWesternDigits(raw).filter { it.isDigit() }
            return when {
                onlyDigits.length != 10 -> "طول کد ملی باید ۱۰ رقم باشد."
                else -> "قالب کد ملی نامعتبر است."
            }
        }

        if (code.toSet().size == 1) return "کد ملی نمی‌تواند از ارقام تکراری تشکیل شود."
        if (!isValid(code)) return "کد ملی نامعتبر است."
        return null
    }

    // ----------------- ابزارهای داخلی -----------------

    /**
     * نرمال‌سازی: تبدیل ارقام فارسی/عربی به انگلیسی + حذف هرچیز غیر عددی.
     * اگر پس از نرمال‌سازی دقیقاً 10 رقم نبود، null.
     */
    private fun normalize(input: String): String? {
        val onlyDigits = mapToWesternDigits(input).filter { it.isDigit() }
        if (onlyDigits.length != 10) return null
        return onlyDigits
    }

    /** نگاشت ارقام فارسی/عربی به انگلیسی (سایر کاراکترها دست‌نخورده می‌مانند). */
    private fun mapToWesternDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
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
