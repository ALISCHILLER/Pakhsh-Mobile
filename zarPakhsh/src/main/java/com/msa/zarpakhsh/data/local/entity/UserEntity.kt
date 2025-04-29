package com.msa.zarpakhsh.data.local.entity



import androidx.room.*
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val token: String
)