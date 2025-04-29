package com.msa.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * ابزار کمکی بهبودیافته برای تبدیل، اعتبارسنجی، قالب‌بندی و محاسبه اعداد فارسی و انگلیسی.
 */
object EnhancedNumberConverter {

    // --- جداول نگاشت ---
    private val westernDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    // برای سهولت، یک Map کامل برای تبدیل به هر دو جهت ایجاد می‌کنیم
    private val westernToPersianMap = westernDigits.zip(persianDigits).toMap()
    private val persianToWesternMap = persianDigits.zip(westernDigits).toMap()

    // --- تبدیل اعداد ---

    /**
     * تبدیل تمام اعداد انگلیسی در متن به معادل فارسی.
     * در صورت null بودن ورودی، رشته خالی برمی‌گرداند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @return متن با اعداد تبدیل شده به فارسی یا رشته خالی اگر ورودی null بود.
     */
    fun convertToPersian(input: String?): String {
        if (input == null) return ""
        return input.map { char -> westernToPersianMap[char] ?: char }.joinToString("")
    }

    /**
     * تبدیل تمام اعداد فارسی در متن به معادل انگلیسی (غربی).
     * در صورت null بودن ورودی، رشته خالی برمی‌گرداند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @return متن با اعداد تبدیل شده به انگلیسی یا رشته خالی اگر ورودی null بود.
     */
    fun convertToWestern(input: String?): String {
        if (input == null) return ""
        return input.map { char -> persianToWesternMap[char] ?: char }.joinToString("")
    }

    // --- اعتبارسنجی ---

    /**
     * بررسی می‌کند که آیا کل رشته فقط و فقط شامل اعداد فارسی یا انگلیسی است یا خیر.
     * کاراکترهای دیگر (حروف، فاصله، علائم) مجاز نیستند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @return true اگر رشته فقط عدد باشد، در غیر این صورت false.
     */
    fun isPurelyNumeric(input: String?): Boolean {
        if (input.isNullOrEmpty()) return false
        // ابتدا به انگلیسی تبدیل کن تا بررسی ساده‌تر شود
        val westernInput = convertToWestern(input)
        return westernInput.all { it.isDigit() }
    }

    /**
     * بررسی می‌کند که آیا رشته یک عدد معتبر (صحیح یا اعشاری) است یا خیر.
     * از اعداد فارسی یا انگلیسی، علامت منفی اختیاری در ابتدا و یک نقطه/ممیز اعشار پشتیبانی می‌کند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @param persianDecimalSeparator کاراکتر ممیز فارسی (معمولاً '٫').
     * @return true اگر رشته یک عدد معتبر باشد، در غیر این صورت false.
     */
    fun isValidNumberString(input: String?, persianDecimalSeparator: Char = '٫'): Boolean {
        if (input.isNullOrEmpty()) return false

        // همه اعداد را به انگلیسی و ممیز فارسی را به نقطه تبدیل کن
        val normalized = buildString {
            var hasDecimalSeparator = false
            input.forEachIndexed { index, char ->
                when {
                    persianToWesternMap.containsKey(char) -> append(persianToWesternMap[char])
                    char == persianDecimalSeparator -> {
                        if (!hasDecimalSeparator) {
                            append('.')
                            hasDecimalSeparator = true
                        } else {
                            return false // دو ممیز مجاز نیست
                        }
                    }
                    char == '.' -> {
                        if (!hasDecimalSeparator) {
                            append('.')
                            hasDecimalSeparator = true
                        } else {
                            return false // دو ممیز مجاز نیست
                        }
                    }
                    char == '-' && index == 0 -> append('-') // علامت منفی فقط در ابتدا مجاز است
                    char.isDigit() -> append(char) // اعداد انگلیسی
                    else -> return false // کاراکتر غیرمجاز
                }
            }
        }
        // رشته نرمال‌شده را امتحان کن که آیا به Double تبدیل می‌شود یا خیر
        return normalized.toDoubleOrNull() != null
    }


    // --- تبدیل و پارس کردن ---

    /**
     * تبدیل متن حاوی اعداد فارسی/انگلیسی به عدد صحیح (Int).
     * ابتدا اعداد فارسی را به انگلیسی تبدیل می‌کند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @return مقدار Int یا null اگر تبدیل ممکن نباشد.
     */
    fun parseInt(input: String?): Int? {
        if (input == null) return null
        // فقط ارقام را استخراج کن (بدون علامت یا اعشار)
        val digitsOnly = convertToWestern(input).filter { it.isDigit() }
        if (digitsOnly.isEmpty() && input.contains('۰')) { // Case for input "۰"
            return 0
        }
        return digitsOnly.toIntOrNull()
    }

    /**
     * تبدیل متن حاوی اعداد فارسی/انگلیسی به عدد اعشاری (Double).
     * از ممیز فارسی ('٫') و انگلیسی ('.') پشتیبانی می‌کند.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @param persianDecimalSeparator کاراکتر ممیز فارسی.
     * @return مقدار Double یا null اگر تبدیل ممکن نباشد.
     */
    fun parseDouble(input: String?, persianDecimalSeparator: Char = '٫'): Double? {
        if (input == null) return null
        val westernInput = convertToWestern(input).replace(persianDecimalSeparator, '.')
        return westernInput.toDoubleOrNull()
    }

    /**
     * تبدیل متن حاوی اعداد فارسی/انگلیسی به BigDecimal برای دقت بالا.
     *
     * @param input متن ورودی (می‌تواند null باشد).
     * @param persianDecimalSeparator کاراکتر ممیز فارسی.
     * @return مقدار BigDecimal یا null اگر تبدیل ممکن نباشد.
     */
    fun parseBigDecimal(input: String?, persianDecimalSeparator: Char = '٫'): BigDecimal? {
        if (input == null) return null
        val westernInput = convertToWestern(input).replace(persianDecimalSeparator, '.')
        return westernInput.toBigDecimalOrNull()
    }

    // --- قالب‌بندی اعداد ---

    /**
     * قالب‌بندی یک عدد با جداکننده‌ی هزارگان و تعداد مشخصی رقم اعشار، با استفاده از اعداد انگلیسی.
     *
     * @param number عددی که باید قالب‌بندی شود.
     * @param decimalPlaces تعداد ارقام اعشار.
     * @param useGrouping آیا از جداکننده هزارگان استفاده شود (مثلاً 1,234.56).
     * @return رشته قالب‌بندی شده با اعداد انگلیسی.
     */
    fun formatNumberWestern(number: Number, decimalPlaces: Int = 2, useGrouping: Boolean = true): String {
        val format = DecimalFormat().apply {
            maximumFractionDigits = decimalPlaces
            minimumFractionDigits = decimalPlaces
            isGroupingUsed = useGrouping
            // اطمینان از استفاده از تنظیمات استاندارد انگلیسی (نقطه برای اعشار، کاما برای گروه)
            decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        }
        return format.format(number)
    }

    /**
     * قالب‌بندی یک عدد با جداکننده‌ی هزارگان و تعداد مشخصی رقم اعشار، با استفاده از اعداد فارسی.
     *
     * @param number عددی که باید قالب‌بندی شود.
     * @param decimalPlaces تعداد ارقام اعشار.
     * @param useGrouping آیا از جداکننده هزارگان استفاده شود (مثلاً ۱٬۲۳۴٫۵۶).
     * @param persianGroupingSeparator جداکننده هزارگان فارسی (معمولاً '٬').
     * @param persianDecimalSeparator جداکننده اعشار فارسی (معمولاً '٫').
     * @return رشته قالب‌بندی شده با اعداد فارسی.
     */
    fun formatNumberPersian(
        number: Number,
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true,
        persianGroupingSeparator: Char = '٬',
        persianDecimalSeparator: Char = '٫'
    ): String {
        // نمادهای فارسی را تعریف می‌کنیم
        val persianSymbols = DecimalFormatSymbols(Locale("fa")).apply { // استفاده از لوکال فارسی
            // می‌توانیم نمادها را دستی هم تنظیم کنیم اگر لوکال دقیقاً چیزی که می‌خواهیم نباشد
            zeroDigit = '۰'
            digit = '#' // این مهم است
            groupingSeparator = persianGroupingSeparator
            decimalSeparator = persianDecimalSeparator
            minusSign = '-' // علامت منفی استاندارد
        }

        // الگو را بر اساس نیاز می‌سازیم
        val pattern = buildString {
            append(if (useGrouping) "#,##0" else "0")
            if (decimalPlaces > 0) {
                append(".") // الگو از نقطه استفاده می‌کند، اما symbols آن را به ممیز فارسی تبدیل می‌کند
                append("0".repeat(decimalPlaces))
            }
        }

        val format = DecimalFormat(pattern, persianSymbols).apply{
            roundingMode = java.math.RoundingMode.HALF_UP // تعیین نحوه گرد کردن
        }

        return format.format(number)
    }


    // --- توابع محاسباتی (استفاده از BigDecimal برای دقت) ---

    /**
     * جمع دو عدد داده شده به صورت رشته. اعداد می‌توانند فارسی یا انگلیسی باشند.
     * از BigDecimal برای دقت بالا استفاده می‌کند.
     *
     * @param numStr1 رشته عدد اول.
     * @param numStr2 رشته عدد دوم.
     * @return نتیجه جمع به صورت BigDecimal یا null اگر ورودی‌ها نامعتبر باشند.
     */
    fun add(numStr1: String?, numStr2: String?): BigDecimal? {
        val num1 = parseBigDecimal(numStr1)
        val num2 = parseBigDecimal(numStr2)
        return if (num1 != null && num2 != null) {
            num1.add(num2)
        } else {
            null
        }
    }

    /**
     * تفریق دو عدد داده شده به صورت رشته.
     * @return نتیجه تفریق به صورت BigDecimal یا null.
     */
    fun subtract(numStr1: String?, numStr2: String?): BigDecimal? {
        val num1 = parseBigDecimal(numStr1)
        val num2 = parseBigDecimal(numStr2)
        return if (num1 != null && num2 != null) {
            num1.subtract(num2)
        } else {
            null
        }
    }

    /**
     * ضرب دو عدد داده شده به صورت رشته.
     * @return نتیجه ضرب به صورت BigDecimal یا null.
     */
    fun multiply(numStr1: String?, numStr2: String?): BigDecimal? {
        val num1 = parseBigDecimal(numStr1)
        val num2 = parseBigDecimal(numStr2)
        return if (num1 != null && num2 != null) {
            num1.multiply(num2)
        } else {
            null
        }
    }

    /**
     * تقسیم دو عدد داده شده به صورت رشته.
     * در صورت تقسیم بر صفر، ArithmeticException پرتاب می‌شود.
     *
     * @param numStr1 رشته عدد اول (مقسوم).
     * @param numStr2 رشته عدد دوم (مقسوم علیه).
     * @param scale تعداد ارقام اعشار برای نتیجه تقسیم (پیش‌فرض 10).
     * @param roundingMode نحوه گرد کردن (پیش‌فرض HALF_UP).
     * @return نتیجه تقسیم به صورت BigDecimal یا null اگر ورودی‌ها نامعتبر باشند.
     * @throws ArithmeticException اگر مقسوم علیه صفر باشد.
     */
    @Throws(ArithmeticException::class)
    fun divide(
        numStr1: String?,
        numStr2: String?,
        scale: Int = 10, // تعداد ارقام اعشار نتیجه
        roundingMode: java.math.RoundingMode = java.math.RoundingMode.HALF_UP
    ): BigDecimal? {
        val num1 = parseBigDecimal(numStr1)
        val num2 = parseBigDecimal(numStr2)

        return if (num1 != null && num2 != null) {
            if (num2 == BigDecimal.ZERO) {
                throw ArithmeticException("Division by zero")
            }
            num1.divide(num2, scale, roundingMode)
        } else {
            null
        }
    }
}