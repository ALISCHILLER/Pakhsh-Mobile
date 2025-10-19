package com.msa.core.common.time

fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val System: Clock = Clock { kotlin.system.getTimeMillis() }
    }
}