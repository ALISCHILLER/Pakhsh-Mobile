package com.msa.core.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException

/**
 * کلاس Currency برای مدیریت مقادیر پولی (ارقام مالی) طراحی شده است.
 * این کلاس از BigDecimal استفاده می‌کند تا دقت بالایی در محاسبات مالی داشته باشد.
 */
class Currency(private var value: BigDecimal) {

    /**
     * سازنده اصلی کلاس که مقدار اولیه را دریافت می‌کند.
     * @param value مقدار اولیه به صورت BigDecimal.
     */
    constructor(value: String) : this(parse(value))

    /**
     * سازنده دوم کلاس که مقدار اولیه را به صورت عددی دریافت می‌کند.
     * @param value مقدار اولیه به صورت Number (مانند Int, Double).
     */
    constructor(value: Number) : this(BigDecimal.valueOf(value.toDouble()).setScale(2, RoundingMode.HALF_UP))

    /**
     * جمع دو مقدار پولی.
     * @param currency مقدار دوم برای جمع.
     * @return نتیجه جمع به صورت یک شیء Currency جدید.
     */
    fun add(currency: Currency): Currency {
        return Currency(value.add(currency.value))
    }

    /**
     * جمع یک مقدار پولی با یک عدد.
     * @param number مقدار عددی برای جمع.
     * @return نتیجه جمع به صورت یک شیء Currency جدید.
     */
    fun add(number: Number): Currency {
        val currency = Currency(number)
        return Currency(value.add(currency.value))
    }

    /**
     * تفریق دو مقدار پولی.
     * @param currency مقدار دوم برای تفریق.
     * @return نتیجه تفریق به صورت یک شیء Currency جدید.
     */
    fun subtract(currency: Currency): Currency {
        return Currency(value.subtract(currency.value))
    }

    /**
     * ضرب دو مقدار پولی.
     * @param currency مقدار دوم برای ضرب.
     * @return نتیجه ضرب به صورت یک شیء Currency جدید.
     */
    fun multiply(currency: Currency): Currency {
        return Currency(value.multiply(currency.value))
    }

    /**
     * تقسیم دو مقدار پولی.
     * @param currency مقدار دوم برای تقسیم.
     * @param scale تعداد اعشار نتیجه تقسیم (پیش‌فرض: 2).
     * @param roundingMode نحوه گرد کردن نتیجه تقسیم (پیش‌فرض: HALF_UP).
     * @return نتیجه تقسیم به صورت یک شیء Currency جدید.
     */
    fun divide(currency: Currency, scale: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): Currency {
        return Currency(value.divide(currency.value, scale, roundingMode))
    }

    /**
     * قالب‌بندی مقدار پولی به صورت رشته با جداکننده هزارها و تعداد اعشار قابل تنظیم.
     * @param decimalPlaces تعداد اعشار مورد نظر (پیش‌فرض: 2).
     * @return مقدار قالب‌بندی‌شده به صورت رشته.
     */
    fun toFormattedString(decimalPlaces: Int = 2): String {
        val symbols = DecimalFormatSymbols.getInstance()
        symbols.groupingSeparator = ',' // جداکننده هزارها
        val pattern = "###,###." + "#".repeat(decimalPlaces) // الگوی قالب‌بندی
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(value.setScale(decimalPlaces, RoundingMode.HALF_UP))
    }

    /**
     * مقایسه دو مقدار پولی.
     * @param other مقدار دوم برای مقایسه.
     * @return نتیجه مقایسه (-1: کوچکتر، 0: مساوی، 1: بزرگتر).
     */
    fun compareTo(other: Currency): Int {
        return value.compareTo(other.value)
    }

    /**
     * بررسی مساوی بودن دو مقدار پولی.
     * @param other مقدار دوم برای مقایسه.
     * @return true اگر مقادیر مساوی باشند، در غیر این صورت false.
     */
    fun isEqualTo(other: Currency): Boolean {
        return value.compareTo(other.value) == 0
    }

    /**
     * بررسی بزرگ‌تر بودن مقدار فعلی نسبت به مقدار دیگر.
     * @param other مقدار دوم برای مقایسه.
     * @return true اگر مقدار فعلی بزرگ‌تر باشد، در غیر این صورت false.
     */
    fun isGreaterThan(other: Currency): Boolean {
        return value.compareTo(other.value) > 0
    }

    /**
     * بررسی کوچک‌تر بودن مقدار فعلی نسبت به مقدار دیگر.
     * @param other مقدار دوم برای مقایسه.
     * @return true اگر مقدار فعلی کوچک‌تر باشد، در غیر این صورت false.
     */
    fun isLessThan(other: Currency): Boolean {
        return value.compareTo(other.value) < 0
    }

    /**
     * اضافه کردن مقدار دیگر به مقدار فعلی (عملیات جمع درجا).
     * @param currency مقداری که باید به مقدار فعلی اضافه شود.
     */
    fun addAssign(currency: Currency) {
        this.value = this.value.add(currency.value)
    }

    /**
     * کم کردن مقدار دیگر از مقدار فعلی (عملیات تفریق درجا).
     * @param currency مقداری که باید از مقدار فعلی کم شود.
     */
    fun subtractAssign(currency: Currency) {
        this.value = this.value.subtract(currency.value)
    }

    /**
     * تبدیل مقدار پولی به عدد Double.
     * @return مقدار پولی به صورت Double.
     */
    fun toDouble(): Double {
        return value.toDouble()
    }

    /**
     * تبدیل مقدار پولی به عدد Int.
     * @return مقدار پولی به صورت Int.
     */
    fun toInt(): Int {
        return value.toInt()
    }

    /**
     * تبدیل مقدار پولی به رشته.
     * @return مقدار پولی به صورت رشته.
     */
    override fun toString(): String {
        return value.toString()
    }

    companion object {
        /**
         * مقادیر ثابت پیش‌فرض برای مقادیر پولی.
         */
        val ZERO = Currency(0) // مقدار صفر
        val ONE = Currency(1) // مقدار یک
        val TEN = Currency(10) // مقدار ده
        val HUNDRED = Currency(100) // مقدار صد

        /**
         * تحلیل و تبدیل رشته ورودی به مقدار BigDecimal.
         * @param value رشته ورودی حاوی مقدار پولی.
         * @return مقدار تحلیل‌شده به صورت BigDecimal.
         * @throws ParseException در صورتی که فرمت ورودی نامعتبر باشد.
         */
        private fun parse(value: String): BigDecimal {
            val cleanedValue = EnhancedNumberConverter.convertToWestern(value).replace(",", "")
            return try {
                BigDecimal(cleanedValue)
            } catch (e: NumberFormatException) {
                throw ParseException("فرمت ورودی نامعتبر است: $value", 0)
            }
        }
    }
}