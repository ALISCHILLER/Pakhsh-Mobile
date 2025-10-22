package com.msa.core.network.util

import io.ktor.http.URLBuilder
import io.ktor.http.buildString

fun buildUrl(base: String, path: String, query: Map<String, Any?>): String {
    val builder = URLBuilder(base)
    val sanitizedPath = listOf(builder.encodedPath.trimEnd('/'), path.trimStart('/'))
        .filter { it.isNotEmpty() }
        .joinToString(separator = "/")
    builder.encodedPath = sanitizedPath
    query.filterValues { it != null }.forEach { (key, value) ->
        builder.parameters.append(key, value.toString())
    }
    return builder.build().toString()
}