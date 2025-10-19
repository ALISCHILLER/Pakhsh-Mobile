package com.msa.core.validation

object EnhancedNumberConverter {
    private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val arabicDigits = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    fun toEnglishDigits(input: String): String {
        val builder = StringBuilder(input.length)
        input.forEach { ch ->
            when (ch) {
                in persianDigits -> builder.append(persianDigits.indexOf(ch))
                in arabicDigits -> builder.append(arabicDigits.indexOf(ch))
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun normalizeSignedNumber(input: String): String {
        val trimmed = input.trim().replace("\u200F", "").replace("\u200E", "")
        return toEnglishDigits(trimmed)
    }
}