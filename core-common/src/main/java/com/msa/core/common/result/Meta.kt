package com.msa.core.common.result

import com.msa.core.common.paging.PageInfo

data class Meta(
    val statusCode: Int? = null,
    val message: String? = null,
    val etag: String? = null,
    val requestId: String? = null,
    val pagination: PageInfo? = null,
    val extras: Map<String, String> = emptyMap(),
    val receivedAtMillis: Long = System.currentTimeMillis(),
    val latencyMillis: Long? = null,
    val fromCache: Boolean = false
) {
    fun merge(other: Meta): Meta = Meta(
        statusCode = other.statusCode ?: statusCode,
        message = other.message ?: message,
        etag = other.etag ?: etag,
        requestId = other.requestId ?: requestId,
        pagination = other.pagination ?: pagination,
        extras = extras + other.extras,
        receivedAtMillis = maxOf(receivedAtMillis, other.receivedAtMillis),
        latencyMillis = other.latencyMillis ?: latencyMillis,
        fromCache = fromCache || other.fromCache
    )

    companion object {
        val EMPTY = Meta()
    }
}