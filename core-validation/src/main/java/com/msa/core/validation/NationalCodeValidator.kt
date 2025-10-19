package com.msa.core.validation

object NationalCodeValidator {
    fun isValid(input: String): Boolean {
        val normalized = EnhancedNumberConverter.toEnglishDigits(input).filter { it.isDigit() }
        if (normalized.length != 10) return false
        if ((0..9).all { normalized.all { digit -> digit == '0' + it } }) return false
        val check = normalized.last().digitToInt()
        val sum = normalized.substring(0, 9)
            .mapIndexed { index, c -> c.digitToInt() * (10 - index) }
            .sum()
        val remainder = sum % 11
        val control = if (remainder < 2) remainder else 11 - remainder
        return control == check
    }
}