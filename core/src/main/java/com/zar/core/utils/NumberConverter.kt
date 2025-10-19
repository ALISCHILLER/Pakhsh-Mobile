package com.zar.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * ابزار بهبودیافته برای تبدیل، اعتبارسنجی، قالب‌بندی و محاسبه اعداد فارسی/انگلیسی/عربی.
 */
object EnhancedNumberConverter {

    // --- جداول نگاشت ---
    private val westernDigits = charArrayOf('0','1','2','3','4','5','6','7','8','9')
    private val persianDigits = charArrayOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    private val arabicDigits  = charArrayOf('٠','١','٢','٣','٤','٥','٦','٧','٨','٩')

    private val westernToPersianMap = westernDigits.zip(persianDigits).toMap()
    private val persianToWesternMap = persianDigits.zip(westernDigits).toMap()
    private val arabicToWesternMap  = arabicDigits.zip(westernDigits).toMap()

    // --- تبدیل اعداد ---

    /** تبدیل اعداد انگلیسی به فارسی (دیگر کاراکترها دست‌نخورده) */
    fun convertToPersian(input: String?): String {
        if (input == null) return ""
        return buildString(input.length) {
            input.forEach { ch -> append(westernToPersianMap[ch] ?: ch) }
        }
    }

    /** تبدیل اعداد فارسی و عربی به انگلیسی (غربی) */
    fun convertToWestern(input: String?): String {
        if (input == null) return ""
        return buildString(input.length) {
            input.forEach { ch ->
                append(
                    persianToWesternMap[ch]
                        ?: arabicToWesternMap[ch]
                        ?: ch
                )
            }
        }
    }

    // --- اعتبارسنجی ---

    /** آیا رشته فقط شامل رقم است؟ (بدون علامت/ممیز) — بعد از تبدیل به انگلیسی چک می‌کنیم */
    fun isPurelyNumeric(input: String?): Boolean {
        if (input.isNullOrEmpty()) return false
        val s = convertToWestern(input)
        return s.isNotEmpty() && s.all { it.isDigit() }
    }

    /**
     * بررسی اعتبار عدد (صحیح یا اعشاری). علامت منفی در ابتدا مجاز، یک ممیز اعشار مجاز.
     * جداکننده‌های گروه فارسی/لاتین نادیده گرفته می‌شوند.
     */
    fun isValidNumberString(input: String?, persianDecimalSeparator: Char = '٫'): Boolean {
        if (input.isNullOrEmpty()) return false
        val normalized = normalizeNumericString(input, persianDecimalSeparator) ?: return false
        return normalized.toDoubleOrNull() != null
    }

    // --- تبدیل و پارس ---

    /** Int با پشتیبانی از علامت و حذف جداکننده گروه */
    fun parseInt(input: String?, persianDecimalSeparator: Char = '٫'): Int? {
        if (input.isNullOrBlank()) return null
        val s = normalizeNumericString(input, persianDecimalSeparator)?.let {
            // عدد صحیح: باید بدون ممیز باشد
            if (it.contains('.')) null else it
        } ?: return null
        return s.toIntOrNull()
    }

    /** Double با پشتیبانی از علامت و جداکننده گروه */
    fun parseDouble(input: String?, persianDecimalSeparator: Char = '٫'): Double? {
        if (input.isNullOrBlank()) return null
        val s = normalizeNumericString(input, persianDecimalSeparator) ?: return null
        return s.toDoubleOrNull()
    }

    /** BigDecimal با پشتیبانی از علامت و جداکننده گروه */
    fun parseBigDecimal(input: String?, persianDecimalSeparator: Char = '٫'): BigDecimal? {
        if (input.isNullOrBlank()) return null
        val s = normalizeNumericString(input, persianDecimalSeparator) ?: return null
        return s.toBigDecimalOrNull()
    }

    // --- قالب‌بندی ---

    /** فرمت انگلیسی (کاما برای هزارگان، نقطه برای اعشار) */
    fun formatNumberWestern(
        number: Number,
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true
    ): String {
        val format = DecimalFormat().apply {
            maximumFractionDigits = decimalPlaces
            minimumFractionDigits = decimalPlaces
            isGroupingUsed = useGrouping
            decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        }
        return format.format(number)
    }

    /** فرمت فارسی (٬ برای هزارگان، ٫ برای اعشار و ارقام فارسی) */
    fun formatNumberPersian(
        number: Number,
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true,
        persianGroupingSeparator: Char = '٬',
        persianDecimalSeparator: Char = '٫'
    ): String {
        val symbols = DecimalFormatSymbols(Locale("fa")).apply {
            zeroDigit = '۰'
            digit = '#'
            groupingSeparator = persianGroupingSeparator
            decimalSeparator = persianDecimalSeparator
            minusSign = '-'
        }
        val pattern = buildString {
            append(if (useGrouping) "#,##0" else "0")
            if (decimalPlaces > 0) {
                append(".")
                append("0".repeat(decimalPlaces))
            }
        }
        val format = DecimalFormat(pattern, symbols).apply {
            roundingMode = java.math.RoundingMode.HALF_UP
        }
        return format.format(number)
    }

    // --- عملیات با BigDecimal ---

    fun add(numStr1: String?, numStr2: String?): BigDecimal? {
        val a = parseBigDecimal(numStr1); val b = parseBigDecimal(numStr2)
        return if (a != null && b != null) a.add(b) else null
    }
    fun subtract(numStr1: String?, numStr2: String?): BigDecimal? {
        val a = parseBigDecimal(numStr1); val b = parseBigDecimal(numStr2)
        return if (a != null && b != null) a.subtract(b) else null
    }
    fun multiply(numStr1: String?, numStr2: String?): BigDecimal? {
        val a = parseBigDecimal(numStr1); val b = parseBigDecimal(numStr2)
        return if (a != null && b != null) a.multiply(b) else null
    }
    @Throws(ArithmeticException::class)
    fun divide(
        numStr1: String?,
        numStr2: String?,
        scale: Int = 10,
        roundingMode: java.math.RoundingMode = java.math.RoundingMode.HALF_UP
    ): BigDecimal? {
        val a = parseBigDecimal(numStr1); val b = parseBigDecimal(numStr2)
        return if (a != null && b != null) {
            if (b == BigDecimal.ZERO) throw ArithmeticException("Division by zero")
            a.divide(b, scale, roundingMode)
        } else null
    }


    // --- Currency helpers ---

    fun parseCurrency(input: String?, scale: Int = 2): Currency? {
        val value = parseBigDecimal(input) ?: return null
        return Currency.fromBigDecimal(value.setScale(scale, java.math.RoundingMode.HALF_UP))
    }

    fun Currency.formatAsPersianCurrency(
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true,
        persianGroupingSeparator: Char = '٬',
        persianDecimalSeparator: Char = '٫'
    ): String = formatNumberPersian(
        number = toBigDecimal(),
        decimalPlaces = decimalPlaces,
        useGrouping = useGrouping,
        persianGroupingSeparator = persianGroupingSeparator,
        persianDecimalSeparator = persianDecimalSeparator
    )

    fun Currency.formatAsWesternCurrency(
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true
    ): String = formatNumberWestern(
        number = toBigDecimal(),
        decimalPlaces = decimalPlaces,
        useGrouping = useGrouping
    )


    // --- ابزار داخلی ---

    /**
     * نرمال‌سازی عدد به رشتهٔ انگلیسی با:
     *  - تبدیل ارقام فارسی/عربی به انگلیسی
     *  - حذف جداکننده هزارگان فارسی/لاتین
     *  - تبدیل ممیز فارسی به نقطه
     *  - پشتیبانی از پرانتز حسابداری و علامت انتهایی
     * خروجی: چیزی شبیه "-1234.56" یا "1234"
     */
    private fun normalizeNumericString(input: String, persianDecimalSeparator: Char): String? {
        var s = convertToWestern(input)
            .replace(" ", "")
            .replace("\u200F", "") // RLM
            .replace("\u200E", "") // LRM
            .replace("٬", "")      // Persian grouping
            .replace(",", "")      // Latin grouping
            .replace(persianDecimalSeparator, '.') // Persian decimal → dot
            .trim()

        if (s.isEmpty()) return null

        // پرانتز حسابداری و علامت منفی انتهایی
        if (s.startsWith("(") && s.endsWith(")")) {
            s = "-${s.substring(1, s.length - 1)}"
        } else if (s.endsWith("-")) {
            s = "-${s.dropLast(1)}"
        }

        // فقط یک علامت منفی در ابتدا مجاز است
        if (s.count { it == '-' } > 1) return null
        if (s.indexOf('-') > 0) return null

        // فقط یک ممیز مجاز است
        if (s.count { it == '.' } > 1) return null

        // حداقل باید رقم داشته باشد
        if (s.none { it.isDigit() }) return null

        return s
    }
}
