package com.msa.persistence.data.auth.remote

import com.msa.core.common.config.AppConfig
import com.msa.core.common.result.Outcome
import com.msa.core.network.client.NetworkRequest
import com.msa.core.network.client.RawApi
import com.msa.persistence.data.auth.dto.LoginRequest
import com.msa.persistence.data.auth.dto.LoginResponse
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import timber.log.Timber

private const val AUTH_LOGIN_PATH = "/auth/login"

class AuthApiImpl(
    private val rawApi: RawApi,
    private val appConfig: AppConfig,
) : AuthApi {

    override suspend fun login(request: LoginRequest): Outcome<LoginResponse> {
        Timber.v(
            "Attempting login for %s on %s",
            request.username,
            appConfig.baseUrl.trimEnd('/') + AUTH_LOGIN_PATH
        )
        return rawApi.execute(
            NetworkRequest(
                method = HttpMethod.Post,
                path = AUTH_LOGIN_PATH,
                body = request,
                parser = { response -> response.body<LoginResponse>() }
            )
        )
    }
}