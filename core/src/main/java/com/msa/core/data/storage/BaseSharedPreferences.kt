package com.msa.core.data.storage


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * کلاس پایه برای مدیریت SharedPreferences.
 * این کلاس از SharedPreferences معمولی یا EncryptedSharedPreferences پشتیبانی می‌کند.
 */
open class BaseSharedPreferences @JvmOverloads constructor(
    context: Context,
    private val prefsName: String,
    isEncrypted: Boolean = false,
    private val gson: Gson = Gson()
) {


    // استفاده از EncryptedSharedPreferences اگر isEncrypted برابر با true باشد
    internal val sharedPreferences: SharedPreferences by lazy {
        if (isEncrypted) {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                prefsName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        }
    }

    // تابع کمکی برای مدیریت خطاها
    private inline fun <T> safeExecute(block: () -> T, onErrorReturn: T): T {
        return try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            onErrorReturn
        }
    }

    // متد برای ذخیره‌سازی یک شیء به صورت JSON
    fun <T> saveObject(key: String, value: T) {
        safeExecute({
            val json = gson.toJson(value)
            sharedPreferences.edit().putString(key, json).apply()
        }, Unit)
    }

    // متد برای خواندن یک شیء از SharedPreferences
    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = sharedPreferences.getString(key, null)
        return safeExecute({
            if (json != null) gson.fromJson(json, clazz) else null
        }, null)
    }

    // متد برای ذخیره‌سازی یک مقدار String
    fun saveString(key: String, value: String) {
        safeExecute({ sharedPreferences.edit().putString(key, value).apply() }, Unit)
    }

    // متد برای خواندن یک مقدار String
    fun getString(key: String, defaultValue: String = ""): String {
        return safeExecute({
            sharedPreferences.getString(key, defaultValue) ?: defaultValue
        }, defaultValue)
    }

    // متد برای ذخیره‌سازی یک مقدار Boolean
    fun saveBoolean(key: String, value: Boolean) {
        safeExecute({ sharedPreferences.edit().putBoolean(key, value).apply() }, Unit)
    }

    // متد برای خواندن یک مقدار Boolean
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return safeExecute({ sharedPreferences.getBoolean(key, defaultValue) }, defaultValue)
    }

    // متد برای ذخیره‌سازی یک مقدار Int
    fun saveInt(key: String, value: Int) {
        safeExecute({ sharedPreferences.edit().putInt(key, value).apply() }, Unit)
    }

    // متد برای خواندن یک مقدار Int
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return safeExecute({ sharedPreferences.getInt(key, defaultValue) }, defaultValue)
    }

    // متد برای ذخیره‌سازی یک مقدار Long
    fun saveLong(key: String, value: Long) {
        safeExecute({ sharedPreferences.edit().putLong(key, value).apply() }, Unit)
    }

    // متد برای خواندن یک مقدار Long
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return safeExecute({ sharedPreferences.getLong(key, defaultValue) }, defaultValue)
    }

    // متد برای ذخیره‌سازی یک مقدار Float
    fun saveFloat(key: String, value: Float) {
        safeExecute({ sharedPreferences.edit().putFloat(key, value).apply() }, Unit)
    }

    // متد برای خواندن یک مقدار Float
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return safeExecute({ sharedPreferences.getFloat(key, defaultValue) }, defaultValue)
    }

    // متد برای حذف یک کلید خاص
    fun removeKey(key: String) {
        safeExecute({ sharedPreferences.edit().remove(key).apply() }, Unit)
    }

    // متد برای حذف تمام داده‌های ذخیره‌شده
    fun clearAll() {
        safeExecute({ sharedPreferences.edit().clear().apply() }, Unit)
    }

    // متد برای بررسی وجود یک کلید
    fun contains(key: String): Boolean {
        return safeExecute({ sharedPreferences.contains(key) }, false)
    }

    // متد برای تبدیل داده‌های ذخیره شده به لیست از شیء
    fun <T> getList(key: String, defaultValue: List<T> = emptyList()): List<T> {
        val json = sharedPreferences.getString(key, null)
        return safeExecute({
            if (json != null) {
                val listType = object : TypeToken<List<T>>() {}.type
                gson.fromJson(json, listType)
            } else {
                defaultValue
            }
        }, defaultValue)
    }

    // متد برای ذخیره لیست از اشیاء
    fun <T> saveList(key: String, value: List<T>) {
        safeExecute({
            val json = gson.toJson(value)
            sharedPreferences.edit().putString(key, json).apply()
        }, Unit)
    }
}

/**
 * کلیدهای مورد استفاده در SharedPreferences
 * برای جلوگیری از اشتباهات تایپی در کلیدها، می‌توان از sealed class استفاده کرد
 */
sealed class PrefKey(val key: String) {
    object UserName : PrefKey("user_name")
    object IsLoggedIn : PrefKey("is_logged_in")
    object UserAge : PrefKey("user_age")
    object Token : PrefKey("token")
}

// نحوه استفاده
//  private val sharedPreferences: BaseSharedPreferences by inject()
//
//fun managePreferences() {
//    // ذخیره‌سازی داده‌ها
//    sharedPreferences.saveString(PrefKey.UserName.key, "JohnDoe")
//    sharedPreferences.saveBoolean(PrefKey.IsLoggedIn.key, true)
//    sharedPreferences.saveInt(PrefKey.UserAge.key, 30)
//
//    // بازیابی داده‌ها
//    val userName = sharedPreferences.getString(PrefKey.UserName.key, "Guest")
//    val isLoggedIn = sharedPreferences.getBoolean(PrefKey.IsLoggedIn.key, false)
//    val userAge = sharedPreferences.getInt(PrefKey.UserAge.key, 0)
//
//    println("User Name: $userName")
//    println("Is Logged In: $isLoggedIn")
//    println("User Age: $userAge")
//
//    // ذخیره و بازیابی لیست از اشیاء
//    sharedPreferences.saveList(PrefKey.Token.key, listOf("token1", "token2"))
//    val tokens = sharedPreferences.getList<String>(PrefKey.Token.key)
//    println("Tokens: $tokens")
//}

//val sharedPreferences: BaseSharedPreferences by inject()
//var userName by remember { mutableStateOf("") }
//
//sharedPreferences.saveString(PrefKey.UserName.key, "JohnDoe")
//userName = sharedPreferences.getString(PrefKey.UserName.key, "Guest")
