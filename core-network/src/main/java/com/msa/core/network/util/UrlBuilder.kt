package com.msa.core.network.util

import io.ktor.http.URLBuilder

fun buildUrl(base: String, path: String, query: Map<String, Any?>): String {
    val builder = URLBuilder(base)
    val baseSegments = builder.pathSegments.filter { it.isNotEmpty() }
    val additionalSegments = path.split('/').map { it.trim() }.filter { it.isNotEmpty() }
    builder.pathSegments = baseSegments + additionalSegments
    query.filterValues { it != null }.forEach { (key, value) ->
        builder.parameters.append(key, value.toString())
    }
    return builder.buildString()
}