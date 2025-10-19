package com.zar.core.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * کلاس Currency برای مقادیر پولی با دقت بالا (BigDecimal).
 * این کلاس immutable است.
 */
class Currency private constructor(private val value: BigDecimal) : Comparable<Currency> {

    /** جمع دو مقدار پولی. */
    fun add(other: Currency): Currency = Currency(value.add(other.value))

    /** جمع با مقدار عددی. */
    fun add(number: Number): Currency = add(of(number))

    /** تفریق. */
    fun subtract(other: Currency): Currency = Currency(value.subtract(other.value))

    /** ضرب. (در محاسبات مالی، ضرب/تقسیم را با دقت مناسب استفاده کنید) */
    fun multiply(other: Currency): Currency = Currency(value.multiply(other.value))

    /** تقسیم. */
    fun divide(
        other: Currency,
        scale: Int = 2,
        roundingMode: RoundingMode = RoundingMode.HALF_UP
    ): Currency = Currency(value.divide(other.value, scale, roundingMode))

    /** عملگرها برای نوشتن تمیزتر */
    operator fun plus(other: Currency) = add(other)
    operator fun minus(other: Currency) = subtract(other)
    operator fun times(other: Currency) = multiply(other)
    operator fun div(other: Currency) = divide(other)

    /** مقایسه */
    override fun compareTo(other: Currency): Int = value.compareTo(other.value)

    /** برابری منطقی روی مقدار stripTrailingZeros (برای 1.0 == 1) */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Currency) return false
        return this.value.stripTrailingZeros() == other.value.stripTrailingZeros()
    }
    override fun hashCode(): Int = value.stripTrailingZeros().hashCode()

    /** تبدیل‌ها */
    fun toBigDecimal(): BigDecimal = value
    fun toDouble(): Double = value.toDouble()
    fun toInt(rounding: RoundingMode = RoundingMode.DOWN): Int = value.setScale(0, rounding).toIntExactOrTruncated()

    /**
     * قالب‌بندی مقدار پولی به صورت رشته با جداکننده هزار و اعشار ثابت.
     * مثال: 12_345.00
     */
    fun toFormattedString(
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true,
        locale: Locale = Locale.US
    ): String {
        val symbols = DecimalFormatSymbols.getInstance(locale).apply {
            // برای اطمینان از نقطه/کاما استاندارد در US
            if (locale == Locale.US) {
                groupingSeparator = ','
                decimalSeparator = '.'
            }
        }
        val df = DecimalFormat().apply {
            isGroupingUsed = useGrouping
            maximumFractionDigits = decimalPlaces
            minimumFractionDigits = decimalPlaces // ← صفرهای اعشار حفظ می‌شوند
            roundingMode = RoundingMode.HALF_UP
            decimalFormatSymbols = symbols
        }
        return df.format(value)
    }

    override fun toString(): String = value.toPlainString()

    // ---------------- Companion ----------------

    companion object {
        /** سازنده ایمن از Number — بدون از دست‌دادن دقت double */
        fun of(number: Number): Currency {
            val bd = when (number) {
                is BigDecimal -> number
                is Long -> BigDecimal.valueOf(number)
                is Int -> BigDecimal.valueOf(number.toLong())
                is Short -> BigDecimal.valueOf(number.toLong())
                is Byte -> BigDecimal.valueOf(number.toLong())
                is Double -> BigDecimal.valueOf(number) // valueOf برای double بی‌خطرتر از BigDecimal(double) است
                is Float -> BigDecimal.valueOf(number.toDouble())
                else -> BigDecimal(number.toString())
            }
            return Currency(bd)
        }

        fun fromBigDecimal(value: BigDecimal): Currency = Currency(value)

        val ZERO: Currency = of(0)
        val ONE: Currency = of(1)
        val TEN: Currency = of(10)
        val HUNDRED: Currency = of(100)

        // کمک: تبدیل امن به Int حتی اگر خارج از بازه Int باشد (truncate)
        private fun BigDecimal.toIntExactOrTruncated(): Int = try {
            this.intValueExact()
        } catch (_: ArithmeticException) {
            this.toBigInteger().toInt()
        }
    }
}
