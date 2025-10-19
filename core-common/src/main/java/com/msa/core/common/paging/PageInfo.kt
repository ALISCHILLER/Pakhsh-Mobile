package com.msa.core.common.paging

data class PageInfo(
    val page: Int? = null,
    val pageSize: Int? = null,
    val nextPage: Int? = null,
    val total: Long? = null
)

data class Paged<T>(
    val items: List<T>,
    val pageInfo: PageInfo
)