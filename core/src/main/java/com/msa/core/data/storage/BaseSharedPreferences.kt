package com.msa.core.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

/**
 * SharedPreferences + (اختیاری) رمزنگاری با Google Tink (جایگزین EncryptedSharedPreferences Deprecated)
 *
 * - اگر isEncrypted=false → مانند SharedPreferences عادی کار می‌کند.
 * - اگر isEncrypted=true → مقدارها را قبل از ذخیره رمزنگاری می‌کند (AEAD/AES256-GCM) و به صورت String(Base64) ذخیره می‌کند.
 *
 * نکته: از AAD مبتنی بر key استفاده می‌شود تا داده به کلید کوپل شود.
 */
open class BaseSharedPreferences @JvmOverloads constructor(
    private val context: Context,
    private val prefsName: String,
    private val isEncrypted: Boolean = false,
    @PublishedApi internal val gson: Gson = Gson() // برای استفاده در inline
) {

    /** آینه‌ی PublishedApi از حالت رمزنگاری برای استفاده‌ی امن در توابع inline public */
    @PublishedApi
    internal val encryptedMode: Boolean = isEncrypted

    /** SharedPreferences پایه (رمزنگاری در سطح داده انجام می‌شود) */
    @PublishedApi
    internal val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    /** موتور رمزنگاری Tink (فقط وقتی isEncrypted=true ساخته می‌شود) */
    private val crypto by lazy { if (isEncrypted) TinkAeadEngine(context, prefsName) else null }

    // ------------ ابزارهای کمکی ------------

    private inline fun <T> runCatchingOrDefault(default: T, block: () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            Timber.w(t, "SharedPreferences operation failed for %s", prefsName)
            default
        }

    private inline fun SharedPreferences.safeEdit(
        commit: Boolean = false,
        crossinline block: SharedPreferences.Editor.() -> Unit,
    ): Boolean {
        return runCatching {
            val editor = edit()
            block(editor)
            if (commit) editor.commit() else {
                editor.apply()
                true
            }
        }.onFailure { t ->
            Timber.e(t, "Failed to persist value in %s", prefsName)
        }.getOrDefault(false)
    }

    // ------------ ذخیره/خواندن آبجکت‌ها ------------

    fun <T> saveObject(key: String, value: T, commit: Boolean = false): Boolean {
        val json = runCatching { gson.toJson(value) }
            .onFailure { Timber.e(it, "Failed to serialise object for key=%s", key) }
            .getOrNull() ?: return false

        val payload = if (isEncrypted) encryptString(json, key) ?: return false else json
        return sharedPreferences.safeEdit(commit) { putString(key, payload) }
    }

    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val raw = sharedPreferences.getString(key, null) ?: return null
        val json = if (isEncrypted) decryptString(raw, key) ?: return null else raw
        return runCatching { gson.fromJson(json, clazz) }
            .onFailure { Timber.e(it, "Failed to parse object for key=%s", key) }
            .getOrNull()
    }

    // inline + reified → باید فقط به اعضای public/@PublishedApi ارجاع دهد
    inline fun <reified T> getObject(key: String): T? {
        val raw = sharedPreferences.getString(key, null) ?: return null
        val json = if (encryptedMode) decryptString(raw, key) ?: return null else raw
        return runCatching {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        }.onFailure { t ->
            Timber.e(t, "Failed to parse object for key=%s", key)
        }.getOrNull()
    }

    // ------------ String ------------

    fun saveString(key: String, value: String, commit: Boolean = false): Boolean {
        val payload = if (isEncrypted) encryptString(value, key) ?: return false else value
        return sharedPreferences.safeEdit(commit) { putString(key, payload) }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        val saved = runCatchingOrDefault(null as String?) { sharedPreferences.getString(key, null) }
        if (saved == null) return defaultValue
        return if (isEncrypted) decryptString(saved, key) ?: defaultValue else saved
    }

    fun getStringOrNull(key: String): String? {
        val saved = runCatchingOrDefault(null as String?) { sharedPreferences.getString(key, null) } ?: return null
        return if (isEncrypted) decryptString(saved, key) else saved
    }

    // ------------ Boolean ------------

    fun saveBoolean(key: String, value: Boolean, commit: Boolean = false): Boolean {
        return if (isEncrypted) {
            val payload = encryptString(value.toString(), key) ?: return false
            sharedPreferences.safeEdit(commit) { putString(key, payload) }
        } else {
            sharedPreferences.safeEdit(commit) { putBoolean(key, value) }
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return if (isEncrypted) {
            val raw = sharedPreferences.getString(key, null) ?: return defaultValue
            decryptString(raw, key)?.toBooleanStrictOrNull() ?: defaultValue
        } else {
            runCatchingOrDefault(defaultValue) { sharedPreferences.getBoolean(key, defaultValue) }
        }
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return if (!sharedPreferences.contains(key)) null else getBoolean(key, false)
    }

    // ------------ Int ------------

    fun saveInt(key: String, value: Int, commit: Boolean = false): Boolean {
        return if (isEncrypted) {
            val payload = encryptString(value.toString(), key) ?: return false
            sharedPreferences.safeEdit(commit) { putString(key, payload) }
        } else {
            sharedPreferences.safeEdit(commit) { putInt(key, value) }
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return if (isEncrypted) {
            val raw = sharedPreferences.getString(key, null) ?: return defaultValue
            decryptString(raw, key)?.toIntOrNull() ?: defaultValue
        } else {
            runCatchingOrDefault(defaultValue) { sharedPreferences.getInt(key, defaultValue) }
        }
    }

    fun getIntOrNull(key: String): Int? {
        return if (!sharedPreferences.contains(key)) null else getInt(key, 0)
    }

    fun incrementInt(key: String, delta: Int = 1, defaultValue: Int = 0, commit: Boolean = false): Int {
        val updated = getInt(key, defaultValue) + delta
        saveInt(key, updated, commit)
        return updated
    }

    // ------------ Long ------------

    fun saveLong(key: String, value: Long, commit: Boolean = false): Boolean {
        return if (isEncrypted) {
            val payload = encryptString(value.toString(), key) ?: return false
            sharedPreferences.safeEdit(commit) { putString(key, payload) }
        } else {
            sharedPreferences.safeEdit(commit) { putLong(key, value) }
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return if (isEncrypted) {
            val raw = sharedPreferences.getString(key, null) ?: return defaultValue
            decryptString(raw, key)?.toLongOrNull() ?: defaultValue
        } else {
            runCatchingOrDefault(defaultValue) { sharedPreferences.getLong(key, defaultValue) }
        }
    }

    fun getLongOrNull(key: String): Long? {
        return if (!sharedPreferences.contains(key)) null else getLong(key, 0L)
    }

    // ------------ Float ------------

    fun saveFloat(key: String, value: Float, commit: Boolean = false): Boolean {
        return if (isEncrypted) {
            val payload = encryptString(value.toString(), key) ?: return false
            sharedPreferences.safeEdit(commit) { putString(key, payload) }
        } else {
            sharedPreferences.safeEdit(commit) { putFloat(key, value) }
        }
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return if (isEncrypted) {
            val raw = sharedPreferences.getString(key, null) ?: return defaultValue
            decryptString(raw, key)?.toFloatOrNull() ?: defaultValue
        } else {
            runCatchingOrDefault(defaultValue) { sharedPreferences.getFloat(key, defaultValue) }
        }
    }

    fun getFloatOrNull(key: String): Float? {
        return if (!sharedPreferences.contains(key)) null else getFloat(key, 0f)
    }

    // ------------ حذف/پاکسازی ------------

    fun removeKey(key: String, commit: Boolean = false): Boolean =
        sharedPreferences.safeEdit(commit) { remove(key) }

    fun removeKeys(vararg keys: String, commit: Boolean = false): Boolean {
        if (keys.isEmpty()) return false
        return sharedPreferences.safeEdit(commit) { keys.forEach { remove(it) } }
    }

    fun clearAll(commit: Boolean = false): Boolean =
        sharedPreferences.safeEdit(commit) { clear() }

    // ------------ متفرقه ------------

    fun contains(key: String): Boolean =
        runCatchingOrDefault(false) { sharedPreferences.contains(key) }

    fun containsAny(vararg keys: String): Boolean {
        if (keys.isEmpty()) return false
        return keys.any { contains(it) }
    }

    fun all(): Map<String, *> = sharedPreferences.all

    // ------------ لیست‌ها ------------

    fun <T> getList(key: String, defaultValue: List<T> = emptyList()): List<T> {
        val raw = sharedPreferences.getString(key, null) ?: return defaultValue
        val json = if (isEncrypted) decryptString(raw, key) ?: return defaultValue else raw
        return runCatching {
            val listType = object : TypeToken<List<T>>() {}.type
            gson.fromJson<List<T>>(json, listType)
        }.onFailure { t ->
            Timber.e(t, "Failed to parse list for key=%s", key)
        }.getOrElse { defaultValue }
    }

    fun <T> saveList(key: String, value: List<T>, commit: Boolean = false): Boolean {
        val json = runCatching { gson.toJson(value) }
            .onFailure { Timber.e(it, "Failed to serialise list for key=%s", key) }
            .getOrNull() ?: return false
        val payload = if (isEncrypted) encryptString(json, key) ?: return false else json
        return sharedPreferences.safeEdit(commit) { putString(key, payload) }
    }

    // ------------ Observers (Flow) ------------

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

    fun edit(commit: Boolean = false, block: SharedPreferences.Editor.() -> Unit): Boolean =
        sharedPreferences.safeEdit(commit, block)

    fun migrateFrom(
        other: BaseSharedPreferences,
        keys: Set<String>? = null,
        clearSource: Boolean = false,
        commit: Boolean = true,
    ): Boolean {
        val entries = if (keys.isNullOrEmpty()) other.all() else other.all().filterKeys { it in keys }
        if (entries.isEmpty()) return false

        val success = sharedPreferences.safeEdit(commit) {
            entries.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, if (isEncrypted) encryptString(value, key) ?: value else value)
                    is Boolean -> {
                        if (isEncrypted) putString(key, encryptString(value.toString(), key) ?: value.toString())
                        else putBoolean(key, value)
                    }
                    is Int -> {
                        if (isEncrypted) putString(key, encryptString(value.toString(), key) ?: value.toString())
                        else putInt(key, value)
                    }
                    is Long -> {
                        if (isEncrypted) putString(key, encryptString(value.toString(), key) ?: value.toString())
                        else putLong(key, value)
                    }
                    is Float -> {
                        if (isEncrypted) putString(key, encryptString(value.toString(), key) ?: value.toString())
                        else putFloat(key, value)
                    }
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val set = value as? Set<String>
                        val json = gson.toJson(set)
                        putString(key, if (isEncrypted) encryptString(json, key) ?: json else json)
                    }
                    null -> remove(key)
                    else -> {
                        runCatching {
                            val json = gson.toJson(value)
                            putString(key, if (isEncrypted) encryptString(json, key) ?: json else json)
                        }.onFailure { Timber.e(it, "Failed to migrate complex value for %s", key) }
                    }
                }
            }
        }

        if (success && clearSource) {
            other.clearAll(commit)
        }
        return success
    }

    /** برای inline بودن observeObject لازم است PublishedApi باشد */
    @PublishedApi
    internal fun <T> observe(
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

    // ------------ Encryption helpers ------------

    @PublishedApi
    internal fun encryptString(value: String, keyAsAad: String): String? {
        val aead = crypto?.aead ?: return null
        return runCatching {
            val ciphertext = aead.encrypt(value.toByteArray(Charsets.UTF_8), keyAsAad.toByteArray())
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        }.onFailure { Timber.e(it, "Encryption failed for key=%s", keyAsAad) }
            .getOrNull()
    }

    @PublishedApi
    internal fun decryptString(stored: String, keyAsAad: String): String? {
        val aead = crypto?.aead ?: return null
        return runCatching {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val plaintext = aead.decrypt(bytes, keyAsAad.toByteArray())
            String(plaintext, Charsets.UTF_8)
        }.onFailure { Timber.e(it, "Decryption failed for key=%s", keyAsAad) }
            .getOrNull()
    }
}

/** موتور رمزنگاری مبتنی بر Google Tink + Android Keystore */
private class TinkAeadEngine(
    context: Context,
    prefsName: String,
) {
    val aead: Aead

    init {
        AeadConfig.register()

        val keysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(
                context,
                "${prefsName}_tink_keyset",
                "${prefsName}_tink_prefs"
            )
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://${prefsName}_tink_master_key")
            .build()
            .keysetHandle

        aead = keysetHandle.getPrimitive(Aead::class.java)
    }
}

/** کلیدهای نمونه */
sealed class PrefKey(val key: String) {
    object UserName : PrefKey("user_name")
    object IsLoggedIn : PrefKey("is_logged_in")
    object UserAge : PrefKey("user_age")
    object Token : PrefKey("token")
}
