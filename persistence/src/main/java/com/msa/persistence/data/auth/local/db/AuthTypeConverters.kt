package com.msa.persistence.data.auth.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class AuthTypeConverters {
    private val gson = Gson()
    private val rolesType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun rolesToJson(roles: List<String>?): String = gson.toJson(roles ?: emptyList(), rolesType)

    @TypeConverter
    fun jsonToRoles(json: String?): List<String> =
        json?.takeIf { it.isNotBlank() }?.let { gson.fromJson<List<String>>(it, rolesType) } ?: emptyList()
}