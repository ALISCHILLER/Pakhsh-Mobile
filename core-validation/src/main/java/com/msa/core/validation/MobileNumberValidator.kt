package com.msa.core.validation

object MobileNumberValidator {
    private val defaultPrefixes = setOf("090", "091", "092", "093", "099")

    fun isValid(input: String, prefixes: Set<String> = defaultPrefixes): Boolean {
        val normalized = EnhancedNumberConverter.toEnglishDigits(input).filter { it.isDigit() }
        if (normalized.length != 11) return false
        return prefixes.any { normalized.startsWith(it) }
    }
}