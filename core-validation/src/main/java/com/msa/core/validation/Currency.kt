package com.msa.core.validation

import java.math.BigDecimal
import java.math.RoundingMode

class Currency private constructor(private val amount: BigDecimal) {
    fun asBigDecimal(): BigDecimal = amount

    fun add(other: Currency): Currency = Currency(amount + other.amount)

    fun subtract(other: Currency): Currency = Currency(amount - other.amount)

    fun multiply(multiplier: BigDecimal): Currency = Currency(amount.multiply(multiplier))

    fun formatAsPersianCurrency(): String = amount.setScale(0, RoundingMode.HALF_UP).toPlainString()

    fun formatAsWesternCurrency(): String = amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    override fun toString(): String = formatAsWesternCurrency()

    companion object {
        fun zero(): Currency = Currency(BigDecimal.ZERO)

        fun of(value: String): Currency = Currency(value.toBigDecimal())

        fun of(value: BigDecimal): Currency = Currency(value)
    }
}