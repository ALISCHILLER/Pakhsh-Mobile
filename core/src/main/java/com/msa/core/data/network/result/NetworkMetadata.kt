package com.msa.core.data.network.result

import com.msa.core.data.network.model.Pagination

/**
 * اطلاعات جانبی مربوط به یک پاسخ شبکه را نگهداری می‌کند.
 */
data class NetworkMetadata(
    val statusCode: Int? = null,
    val status: String? = null,
    val message: String? = null,
    val pagination: Pagination? = null,
    val attemptedRetries: Int = 0,
    val maxRetries: Int = 0,
)