package com.zar.zarpakhsh.data.models

import com.zar.zarpakhsh.domain.entities.Customer

data class CustomerResponse(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postalCode: String?
)