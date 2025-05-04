package com.zar.zarpakhsh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("Customer")
data class CustomerModelEntity(
    @PrimaryKey(autoGenerate = false)
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
