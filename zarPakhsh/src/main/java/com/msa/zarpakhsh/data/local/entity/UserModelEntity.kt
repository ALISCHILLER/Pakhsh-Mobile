package com.msa.zarpakhsh.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user")
data class UserModelEntity(
    @PrimaryKey
    val id:String,
    val customerCode:String,
    val customerName:String?,
    val mobile:String?,
    val phone:String?,
    val center:String?,
    val nationalCode:String?,
    val password:String?,
    val salt:String?,
)
