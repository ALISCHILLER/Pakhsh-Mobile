package com.msa.core.network.cache

import com.msa.core.common.result.Meta
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class HttpCacheRepositoryImpl : HttpCacheRepository {
    private val etags = ConcurrentHashMap<String, String>()
    private val bodies = ConcurrentHashMap<String, CachedEntry<Any>>()

    private fun key(method: String, url: String) = "$method|$url"
    private fun extractUrl(key: String): String = key.substringAfterLast('|', "")

    override fun readEtag(method: String, url: String): String? = etags[key(method, url)]

    override fun writeEtag(method: String, url: String, etag: String) {
        etags[key(method, url)] = etag
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> read(method: String, url: String): CachedEntry<T>? =
        bodies[key(method, url)]
            ?.let { entry ->
                entry.copy(meta = entry.meta.copy(fromCache = true)) as CachedEntry<T>
            }

    override fun <T> write(method: String, url: String, value: T, meta: Meta) {
        bodies[key(method, url)] = CachedEntry(value as Any, meta.copy(fromCache = false))
    }

    override fun invalidate(urlPrefix: String?) {
        if (urlPrefix.isNullOrBlank()) {
            etags.clear()
            bodies.clear()
            return
        }
        val matcher = buildMatcher(urlPrefix)
        bodies.keys.removeIf { cacheKey ->
            val shouldRemove = matcher(extractUrl(cacheKey))
            if (shouldRemove) {
                etags.remove(cacheKey)
            }
            shouldRemove
        }
        etags.keys.removeIf { matcher(extractUrl(it)) }
    }

    private fun buildMatcher(prefix: String): (String) -> Boolean {
        return if (prefix.startsWith("http", ignoreCase = true)) {
            { url -> url.startsWith(prefix) }
        } else {
            val normalized = if (prefix.startsWith('/')) prefix else "/$prefix"
            { url ->
                val path = runCatching { URI(url).path }.getOrNull() ?: url
                path.startsWith(normalized)
            }
        }
    }
}