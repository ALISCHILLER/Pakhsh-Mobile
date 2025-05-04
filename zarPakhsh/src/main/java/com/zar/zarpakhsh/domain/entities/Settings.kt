package com.zar.zarpakhsh.domain.entities

data class Settings(
    val id: String,
    val name: String,
    val value: String,
    val type: String,
    val description: String,
    val status: Int,
    val createdAt: String,
    val updatedAt: String
)
