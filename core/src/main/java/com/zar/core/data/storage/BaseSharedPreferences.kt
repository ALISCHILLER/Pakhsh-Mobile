package com.zar.core.data.storage


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber


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
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } else {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        }
    }

    // تابع کمکی برای مدیریت خطاها
    private inline fun <T> runCatchingOrDefault(default: T, block: () -> T): T {
        return try {
            block()
        } catch (throwable: Throwable) {
            Timber.w(throwable, "SharedPreferences operation failed for %s", prefsName)
            default
        }
    }

    private inline fun SharedPreferences.safeEdit(
        commit: Boolean = false,
        crossinline block: SharedPreferences.Editor.() -> Unit,
    ): Boolean {
        return runCatching {
            val editor = edit()
            block.invoke(editor)
            if (commit) {
                editor.commit()
            } else {
                editor.apply()
                true
            }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to persist value in %s", prefsName)
        }.getOrDefault(false)
    }

    fun <T> saveObject(key: String, value: T, commit: Boolean = false): Boolean {
        val json = runCatching { gson.toJson(value) }
            .onFailure { Timber.e(it, "Failed to serialise object for key=%s", key) }
            .getOrNull()
            ?: return false

        return sharedPreferences.safeEdit(commit) { putString(key, json) }
    }


    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = sharedPreferences.getString(key, null) ?: return null
        return runCatching {
            gson.fromJson(json, clazz)
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to parse object for key=%s", key)
        }.getOrNull()
    }

    inline fun <reified T> getObject(key: String): T? {
        val json = sharedPreferences.getString(key, null) ?: return null
        return runCatching {
            gson.fromJson(json, object : TypeToken<T>() {}.type)
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to parse object for key=%s", key)
        }.getOrNull()
    }

    fun saveString(key: String, value: String, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { putString(key, value) }
    }


    fun getString(key: String, defaultValue: String = ""): String {
        return runCatchingOrDefault(defaultValue) {
            sharedPreferences.getString(key, defaultValue) ?: defaultValue
        }
    }

    fun getStringOrNull(key: String): String? {
        return runCatchingOrDefault(null) { sharedPreferences.getString(key, null) }
    }

    fun saveBoolean(key: String, value: Boolean, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { putBoolean(key, value) }
    }


    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return runCatchingOrDefault(defaultValue) { sharedPreferences.getBoolean(key, defaultValue) }
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return if (sharedPreferences.contains(key)) {
            runCatchingOrDefault(null) { sharedPreferences.getBoolean(key, false) }
        } else {
            null
        }
    }

    fun saveInt(key: String, value: Int, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { putInt(key, value) }
    }


    fun getInt(key: String, defaultValue: Int = 0): Int {
        return runCatchingOrDefault(defaultValue) { sharedPreferences.getInt(key, defaultValue) }
    }

    fun getIntOrNull(key: String): Int? {
        return if (sharedPreferences.contains(key)) {
            runCatchingOrDefault(null) { sharedPreferences.getInt(key, 0) }
        } else {
            null
        }
    }

    fun incrementInt(key: String, delta: Int = 1, defaultValue: Int = 0, commit: Boolean = false): Int {
        val updated = getInt(key, defaultValue) + delta
        saveInt(key, updated, commit)
        return updated
    }

    fun saveLong(key: String, value: Long, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { putLong(key, value) }
    }


    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return runCatchingOrDefault(defaultValue) { sharedPreferences.getLong(key, defaultValue) }
    }

    fun getLongOrNull(key: String): Long? {
        return if (sharedPreferences.contains(key)) {
            runCatchingOrDefault(null) { sharedPreferences.getLong(key, 0L) }
        } else {
            null
        }
    }

    fun saveFloat(key: String, value: Float, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { putFloat(key, value) }
    }


    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return runCatchingOrDefault(defaultValue) { sharedPreferences.getFloat(key, defaultValue) }
    }

    fun getFloatOrNull(key: String): Float? {
        return if (sharedPreferences.contains(key)) {
            runCatchingOrDefault(null) { sharedPreferences.getFloat(key, 0f) }
        } else {
            null
        }
    }

    fun removeKey(key: String, commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { remove(key) }
    }

    fun removeKeys(vararg keys: String, commit: Boolean = false): Boolean {
        if (keys.isEmpty()) return false
        return sharedPreferences.safeEdit(commit) {
            keys.forEach { remove(it) }
        }
    }

    fun clearAll(commit: Boolean = false): Boolean {
        return sharedPreferences.safeEdit(commit) { clear() }
    }


    fun contains(key: String): Boolean {
        return runCatchingOrDefault(false) { sharedPreferences.contains(key) }
    }

    fun containsAny(vararg keys: String): Boolean {
        if (keys.isEmpty()) return false
        return keys.any { contains(it) }
    }

    fun containsAny(vararg keys: String): Boolean {
        if (keys.isEmpty()) return false
        return keys.any { contains(it) }
    }

    fun all(): Map<String, *> = sharedPreferences.all
    fun <T> getList(key: String, defaultValue: List<T> = emptyList()): List<T> {
        val json = sharedPreferences.getString(key, null) ?: return defaultValue
        return runCatching {
            val listType = object : TypeToken<List<T>>() {}.type
            gson.fromJson<List<T>>(json, listType)
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to parse list for key=%s", key)
        }.getOrElse { defaultValue }
    }

    fun <T> saveList(key: String, value: List<T>, commit: Boolean = false): Boolean {
        val json = runCatching { gson.toJson(value) }
            .onFailure { Timber.e(it, "Failed to serialise list for key=%s", key) }
            .getOrNull()
            ?: return false
        return sharedPreferences.safeEdit(commit) { putString(key, json) }
    }

    fun <T> observeObject(
        key: String,
        clazz: Class<T>,
        emitInitial: Boolean = true,
    ): Flow<T?> = observe(key, emitInitial) { getObject(key, clazz) }

    inline fun <reified T> observeObject(
        key: String,
        emitInitial: Boolean = true,
    ): Flow<T?> = observe(key, emitInitial) { getObject<T>(key) }

    fun observeString(
        key: String,
        defaultValue: String = "",
        emitInitial: Boolean = true,
    ): Flow<String> = observe(key, emitInitial) { getString(key, defaultValue) }

    fun observeBoolean(
        key: String,
        defaultValue: Boolean = false,
        emitInitial: Boolean = true,
    ): Flow<Boolean> = observe(key, emitInitial) { getBoolean(key, defaultValue) }

    fun observeInt(
        key: String,
        defaultValue: Int = 0,
        emitInitial: Boolean = true,
    ): Flow<Int> = observe(key, emitInitial) { getInt(key, defaultValue) }

    fun observeLong(
        key: String,
        defaultValue: Long = 0L,
        emitInitial: Boolean = true,
    ): Flow<Long> = observe(key, emitInitial) { getLong(key, defaultValue) }

    fun observeFloat(
        key: String,
        defaultValue: Float = 0f,
        emitInitial: Boolean = true,
    ): Flow<Float> = observe(key, emitInitial) { getFloat(key, defaultValue) }

    fun edit(commit: Boolean = false, block: SharedPreferences.Editor.() -> Unit): Boolean {
        return sharedPreferences.safeEdit(commit, block)
    }

    fun migrateFrom(
        other: BaseSharedPreferences,
        keys: Set<String>? = null,
        clearSource: Boolean = false,
        commit: Boolean = true,
    ): Boolean {
        val entries = if (keys.isNullOrEmpty()) {
            other.all()
        } else {
            other.all().filterKeys { it in keys }
        }
        if (entries.isEmpty()) return false

        val success = sharedPreferences.safeEdit(commit) {
            entries.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        putStringSet(key, value as? Set<String>)
                    }
                    null -> remove(key)
                    else -> {
                        runCatching { putString(key, gson.toJson(value)) }
                            .onFailure { Timber.e(it, "Failed to migrate complex value for %s", key) }
                    }
                }
            }
        }

        if (success && clearSource) {
            other.clearAll(commit)
        }
        return success
    }

    private fun <T> observe(
        key: String,
        emitInitial: Boolean,
        supplier: () -> T,
    ): Flow<T> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == null || changedKey == key) {
                    runCatching { supplier() }
                        .onSuccess { trySend(it).isSuccess }
                        .onFailure { Timber.e(it, "Failed to emit preference for key=%s", key) }
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
            if (emitInitial) {
                runCatching { supplier() }
                    .onSuccess { trySend(it).isSuccess }
                    .onFailure { Timber.e(it, "Failed to emit initial preference for key=%s", key) }
            }
            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.conflate().distinctUntilChanged()
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

