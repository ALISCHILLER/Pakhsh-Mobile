package com.msa.core.common.paging

data class PageInfo(
    val page: Int? = null,
    val pageSize: Int? = null,
    val nextPage: Int? = null,
    val previousPage: Int? = null,
    val total: Long? = null,
    val nextCursor: String? = null,
    val previousCursor: String? = null
)

data class Paged<T>(
    val items: List<T>,
    val pageInfo: PageInfo
)