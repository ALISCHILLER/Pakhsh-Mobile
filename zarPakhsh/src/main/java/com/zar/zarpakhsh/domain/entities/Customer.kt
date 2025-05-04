package com.zar.zarpakhsh.domain.entities

data class Customer(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String
)
