package com.msa.core.validation

object MobileNumberValidator {
    private val defaultPrefixes = setOf("090", "091", "092", "093", "099")

    fun isValid(input: String, prefixes: Set<String> = defaultPrefixes): Boolean {
        val normalized = EnhancedNumberConverter.toEnglishDigits(input).filter { it.isDigit() }
        if (normalized.length != 11) return false
        return prefixes.any { normalized.startsWith(it) }
    }
    fun parse(input: String, prefixes: Set<String> = defaultPrefixes): MobileNumber? {
        val normalized = EnhancedNumberConverter.toEnglishDigits(input).filter { it.isDigit() }
        if (normalized.length != 11) return null
        val prefix = prefixes.firstOrNull { normalized.startsWith(it) } ?: return null
        return MobileNumber(original = input, normalized = normalized, prefix = prefix)
    }
}

data class MobileNumber(
    val original: String,
    val normalized: String,
    val prefix: String,
) {
    fun asInternational(countryCode: String = "+98"): String =
        countryCode + normalized.removePrefix("0")
}