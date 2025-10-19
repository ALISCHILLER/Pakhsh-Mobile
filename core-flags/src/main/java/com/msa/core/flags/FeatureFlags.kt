package com.msa.core.flags

interface FeatureFlags {
    fun isEnabled(key: String, default: Boolean = false): Boolean
    fun getVariant(key: String, default: String? = null): String?
}

inline fun <T> gate(flag: String, flags: FeatureFlags, block: () -> T, fallback: () -> T): T =
    if (flags.isEnabled(flag)) block() else fallback()