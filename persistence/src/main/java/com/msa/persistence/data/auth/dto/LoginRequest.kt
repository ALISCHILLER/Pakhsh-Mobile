package com.msa.persistence.data.auth.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    @SerialName("app_flavor") val appFlavor: String,
    @SerialName("device_id") val deviceId: String? = null,
)
