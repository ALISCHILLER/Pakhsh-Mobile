package com.zar.zarpakhsh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "customers")
data class CustomerModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val mobile: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: String
)
