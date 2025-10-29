package com.msa.persistence.common.prefs

import com.msa.core.common.result.Meta
import com.msa.core.storage.prefs.BaseSharedPreferences
import com.msa.persistence.data.auth.local.entity.AuthSessionEntity
import timber.log.Timber

class SessionPrefs(
    private val preferences: BaseSharedPreferences,
) {

    private companion object {
        const val KEY_SESSION = "auth_session"
        const val KEY_META = "auth_session_meta"
    }

    fun readSession(): AuthSessionEntity? =
        preferences.getObject(KEY_SESSION, AuthSessionEntity::class.java)

    fun readMeta(): Meta? =
        preferences.getObject(KEY_META, Meta::class.java)

    fun writeSession(session: AuthSessionEntity) {
        val persisted = preferences.saveObject(KEY_SESSION, session, commit = true)
        if (!persisted) {
            Timber.w("Failed to persist auth session to shared preferences")
        }
    }

    fun writeMeta(meta: Meta) {
        val persisted = preferences.saveObject(KEY_META, meta, commit = true)
        if (!persisted) {
            Timber.w("Failed to persist auth session metadata")
        }
    }

    fun clearSession() {
        preferences.removeKeys(KEY_SESSION, KEY_META, commit = true)
    }

    fun clearMeta() {
        preferences.removeKey(KEY_META, commit = true)
    }
}