package com.msa.zarpakhsh.data.local.storage

import android.content.Context
import com.msa.core.data.storage.BaseSharedPreferences
import com.msa.core.data.storage.PrefKey
import com.msa.zarpakhsh.domain.entities.User

/**
 * کلاس مدیریت داده‌های محلی برای احراز هویت.
 * این کلاس از SharedPreferences برای ذخیره و بازیابی اطلاعات کاربر استفاده می‌کند.
 */
class LocalDataSourceAuth(
    private val sharedPreferences: BaseSharedPreferences
) {

    /**
     * ذخیره اطلاعات کاربر در حافظه محلی.
     *
     * @param user اطلاعات کاربر شامل نام کاربری، ایمیل و توکن.
     */
    fun saveUser(user: User) {
        sharedPreferences.saveString(PrefKey.UserName.key, user.username)
        sharedPreferences.saveString(PrefKey.Token.key, user.token)
    }

    /**
     * خواندن اطلاعات کاربر از حافظه محلی.
     *
     * @return اطلاعات کاربر یا null اگر داده‌ای وجود نداشته باشد.
     */
    fun getUser(): User? {
        val username = sharedPreferences.getString(PrefKey.UserName.key, "")
        val token = sharedPreferences.getString(PrefKey.Token.key, "")
        return if (username.isNotEmpty() && token.isNotEmpty()) {
            User(username = username, token = token)
        } else {
            null
        }
    }

    /**
     * بررسی وضعیت ورود کاربر.
     *
     * @return true اگر کاربر وارد شده باشد، false در غیر این صورت.
     */
    fun isLoggedIn(): Boolean {
        val token = sharedPreferences.getString(PrefKey.Token.key, "")
        return token.isNotEmpty()
    }

    /**
     * خروج کاربر و پاک کردن اطلاعات ذخیره‌شده.
     */
    fun logout() {
        sharedPreferences.removeKey(PrefKey.UserName.key)
        sharedPreferences.removeKey(PrefKey.Token.key)
    }

    /**
     * ذخیره وضعیت ورود کاربر.
     *
     * @param isLoggedIn وضعیت ورود کاربر (true برای ورود، false برای خروج).
     */
    fun setIsLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.saveBoolean(PrefKey.IsLoggedIn.key, isLoggedIn)
    }

    /**
     * خواندن وضعیت ورود کاربر.
     *
     * @return وضعیت ورود کاربر (true برای ورود، false برای خروج).
     */
    fun getIsLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(PrefKey.IsLoggedIn.key, false)
    }
}