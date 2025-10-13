package com.zar.zarpakhsh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "product_units")
data class ProductUnitEntity(
    @PrimaryKey
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
