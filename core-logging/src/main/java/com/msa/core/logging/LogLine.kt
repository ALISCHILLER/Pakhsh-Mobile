package com.msa.core.logging

import kotlinx.serialization.Serializable

@Serializable
data class LogLine(
    val ts: String,
    val level: String,
    val tag: String?,
    val thread: String,
    val msg: String,
    val userId: String? = null,
    val traceId: String? = null,
    val endpoint: String? = null,
    val status: Int? = null,
    val requestId: String? = null
)