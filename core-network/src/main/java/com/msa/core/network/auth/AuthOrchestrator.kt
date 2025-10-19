package com.msa.core.network.auth

interface AuthOrchestrator {
    suspend fun refresh(oldAccess: String?, oldRefresh: String?): Pair<String, String?>?
    fun shouldAttach(url: String): Boolean = true
    fun authHeader(access: String): String = "Bearer $access"
}