package com.msa.core.common.result

import com.msa.core.common.paging.PageInfo

data class Meta(
    val statusCode: Int? = null,
    val etag: String? = null,
    val requestId: String? = null,
    val pagination: PageInfo? = null,
    val receivedAtMillis: Long = System.currentTimeMillis()
)