package com.msa.zarpakhsh.data.local.storage

import android.content.Context
import com.msa.core.data.storage.BaseSharedPreferences
import com.msa.zarpakhsh.domain.entities.User


class LocalDataSourceAuth(context: Context)  {

    private val sharedPreferences = BaseSharedPreferences(
        context = context,
        prefsName = "auth_prefs",
        isEncrypted = true
    )

    suspend fun saveAuthToken(token: String) {
        sharedPreferences.saveString("auth_token", token)
    }

    suspend fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", "")
    }

    suspend fun saveUser(user: User) {
        sharedPreferences.saveObject("user", user)
    }

    suspend fun getUser(): User? {
        return sharedPreferences.getObject("user", User::class.java)
    }

    suspend fun clearUserData() {
        sharedPreferences.clearAll()
    }

    fun observeIsLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

}