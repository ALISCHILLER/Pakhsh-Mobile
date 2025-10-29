package com.msa.persistence.data.auth.remote

import com.msa.core.common.result.Outcome
import com.msa.persistence.data.auth.dto.LoginRequest
import com.msa.persistence.data.auth.dto.LoginResponse

interface AuthApi {
    suspend fun login(request: LoginRequest): Outcome<LoginResponse>
}