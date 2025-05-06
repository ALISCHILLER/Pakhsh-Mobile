package com.zar.core.data.network.model

/**
 * مدل استاندارد پاسخ API که شامل اطلاعات وضعیت، داده‌ها و خطاهای منطقی است.
 */
data class ApiResponse<T>(
    val code: Int,
    val status: String,
    val data: T?,
    val message: String = "",
    val hasError: Boolean = false,
    val pagination: Pagination? = null
)

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val itemsPerPage: Int,
    val totalItems: Long
)