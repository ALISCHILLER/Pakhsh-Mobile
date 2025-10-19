package com.msa.core.logging

private val authorizationHeaderRegex = Regex("(?i)(Authorization\\s*:\\s*(?:Bearer|Basic)\\s+)([A-Za-z0-9._\\-+/=]+)")
private val bearerTokenRegex = Regex("(?i)(bearer\\s+)([A-Za-z0-9._\\-+/=]+)")
private val querySecretRegex = Regex("(?i)((?:token|access_token|refresh_token|api_key|apikey)=)([^&\\s]+)")
private val jsonSecretRegex = Regex("(?i)\\\"(access_token|refresh_token|password|secret|authorization|api_key|apikey)\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")

fun maskSecrets(input: String): String {
    var sanitized = input
    sanitized = authorizationHeaderRegex.replace(sanitized) { match ->
        "${match.groupValues[1]}***"
    }
    sanitized = bearerTokenRegex.replace(sanitized) { match ->
        "${match.groupValues[1]}***"
    }
    sanitized = querySecretRegex.replace(sanitized) { match ->
        "${match.groupValues[1]}***"
    }
    sanitized = jsonSecretRegex.replace(sanitized) { match ->
        "\"${match.groupValues[1]}\":\"***\""
    }
    return sanitized
}