package com.zar.zarpakhsh.domain.entities

data class Product(
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
