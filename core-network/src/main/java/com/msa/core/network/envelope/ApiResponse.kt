package com.msa.core.network.envelope

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("hasError") val hasError: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("meta") val meta: Map<String, String>? = null
)