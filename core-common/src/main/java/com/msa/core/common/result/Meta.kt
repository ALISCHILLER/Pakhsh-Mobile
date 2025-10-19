package com.msa.core.common.result

import com.zar.core.common.paging.PageInfo

data class Meta(
    val statusCode: Int? = null,
    val etag: String? = null,
    val requestId: String? = null,
    val pagination: PageInfo? = null,
    val receivedAtMillis: Long = System.currentTimeMillis()
)