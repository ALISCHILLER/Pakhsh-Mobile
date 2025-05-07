package com.zar.zarpakhsh.data.local.entity

import androidx.room.Entity

@Entity("product_groups")
data class ProductGroupEntity(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val category: String,
    val stockQuantity: Int,
    val rating: Float,
    val isFavorite: Boolean
)