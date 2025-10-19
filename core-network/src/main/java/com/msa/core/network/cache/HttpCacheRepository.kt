package com.msa.core.network.cache

import com.msa.core.common.result.Meta

data class CachedEntry<T>(val value: T, val meta: Meta)

interface HttpCacheRepository {
    fun readEtag(method: String, url: String): String?
    fun writeEtag(method: String, url: String, etag: String)
    fun <T> read(method: String, url: String): CachedEntry<T>?
    fun <T> write(method: String, url: String, value: T, meta: Meta)
    fun invalidate(urlPrefix: String? = null)
}