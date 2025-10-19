package com.msa.core.network.error

import com.msa.core.common.error.AppError
import com.msa.core.common.text.StringProvider
import com.msa.core.network.config.NetHeaders
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentConvertException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException as JvmSocketTimeout
import io.ktor.client.network.sockets.SocketTimeoutException

interface ErrorMapper {
    fun fromException(t: Throwable, endpoint: String? = null): AppError
    fun fromHttp(code: Int, body: String?, endpoint: String, headers: Map<String, String> = emptyMap()): AppError
}

class ErrorMapperImpl(
    private val strings: StringProvider
) : ErrorMapper {

    override fun fromException(t: Throwable, endpoint: String?): AppError = when (t) {
        is SocketTimeoutException, is JvmSocketTimeout -> AppError.Network(
            strings.get(NetworkStringRes.ERR_TIMEOUT),
            HttpStatusCode.RequestTimeout.value,
            isTimeout = true,
            cause = t
        )
        is IOException -> AppError.Network(
            strings.get(NetworkStringRes.ERR_NETWORK),
            null,
            isConnectivity = true,
            cause = t
        )
        is ContentConvertException, is SerializationException -> AppError.Parsing(
            strings.get(NetworkStringRes.ERR_PARSING),
            raw = null,
            endpoint = endpoint
        )
        is ResponseException -> {
            val response = t.response
            val endpointUrl = endpoint ?: response.request.url.toString()
            val headerMap = response.headers.asStringMap()
            val body = runCatching { response.bodyAsText() }.getOrNull()
            fromHttp(response.status.value, body, endpointUrl, headerMap)
        }
        else -> AppError.Unknown(strings.get(NetworkStringRes.ERR_UNKNOWN), t, endpoint)
    }

    override fun fromHttp(code: Int, body: String?, endpoint: String, headers: Map<String, String>): AppError {
        val requestId = headers.requestId()
        return when (code) {
            HttpStatusCode.RequestTimeout.value -> AppError.Network(
                strings.get(NetworkStringRes.ERR_TIMEOUT),
                code,
                isTimeout = true
            )
            HttpStatusCode.TooManyRequests.value -> AppError.Server(
                strings.get(NetworkStringRes.ERR_CLIENT),
                code,
                body,
                endpoint,
                requestId,
                headers
            )
            401, 403 -> AppError.Auth(strings.get(NetworkStringRes.ERR_AUTH), "HTTP_$code", endpoint)
            in 400..499 -> AppError.Server(
                strings.get(NetworkStringRes.ERR_CLIENT),
                code,
                body,
                endpoint,
                requestId,
                headers
            )
            in 500..599 -> AppError.Server(
                strings.get(NetworkStringRes.ERR_SERVER),
                code,
                body,
                endpoint,
                requestId,
                headers
            )
            else -> AppError.Unknown(strings.get(NetworkStringRes.ERR_UNKNOWN), null, endpoint)
        }
    }
}

private fun Headers.asStringMap(): Map<String, String> =
    entries().associate { (key, values) -> key to values.joinToString(separator = ",") }

private fun Map<String, String>.requestId(): String? =
    entries.firstOrNull { (key, _) -> key.equals(NetHeaders.X_REQUEST_ID, ignoreCase = true) }?.value