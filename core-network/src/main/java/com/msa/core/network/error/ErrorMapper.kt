package com.msa.core.network.error

import com.msa.core.common.error.AppError
import com.msa.core.common.text.StringProvider
import com.msa.core.network.config.NetHeaders
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException as JvmSocketTimeout
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.runBlocking


interface ErrorMapper {
    fun fromException(t: Throwable, endpoint: String? = null): AppError
    fun fromHttp(code: Int, body: String?, endpoint: String, headers: Map<String, String> = emptyMap()): AppError
}

class ErrorMapperImpl(
    private val strings: StringProvider
) : ErrorMapper {

    override fun fromException(t: Throwable, endpoint: String?): AppError = when (t) {
        is SocketTimeoutException, is JvmSocketTimeout -> AppError.Timeout(
            message = strings.get(NetworkStringRes.ERR_TIMEOUT),
            errorCode = HttpStatusCode.RequestTimeout.value.toString(),
            cause = t,
            endpoint = endpoint
        )
        is IOException -> AppError.Network(
            message = strings.get(NetworkStringRes.ERR_NETWORK),
            isConnectivity = true,
            cause = t,
            endpoint = endpoint
        )
        is ContentConvertException, is SerializationException -> AppError.Parsing(
            message = strings.get(NetworkStringRes.ERR_PARSING),
            endpoint = endpoint
        )
        is ResponseException -> {
            val response = t.response
            val endpointUrl = endpoint ?: response.call.request.url.toString()
            val headerMap = response.headers.asStringMap()
            val body = runCatching { runBlocking { response.bodyAsText() } }.getOrNull()
            fromHttp(response.status.value, body, endpointUrl, headerMap)
        }
        else -> AppError.Unknown(strings.get(NetworkStringRes.ERR_UNKNOWN), cause = t, endpoint = endpoint)
    }

    override fun fromHttp(code: Int, body: String?, endpoint: String, headers: Map<String, String>): AppError {
        val requestId = headers.requestId()
        return when (code) {
            HttpStatusCode.RequestTimeout.value -> AppError.Timeout(
                message = strings.get(NetworkStringRes.ERR_TIMEOUT),
                errorCode = code.toString(),
                endpoint = endpoint
            )
            HttpStatusCode.TooManyRequests.value -> AppError.Server(
                message = strings.get(NetworkStringRes.ERR_CLIENT),
                statusCode = code,
                body = body,
                endpoint = endpoint,
                requestId = requestId,
                headers = headers
            )
            401, 403 -> AppError.Auth(
                message = strings.get(NetworkStringRes.ERR_AUTH),
                reason = "HTTP_$code",
                endpoint = endpoint
            )

            in 400..499 -> AppError.Server(
                message = strings.get(NetworkStringRes.ERR_CLIENT),
                statusCode = code,
                body = body,
                endpoint = endpoint,
                requestId = requestId,
                headers = headers
            )
            in 500..599 -> AppError.Server(
                message = strings.get(NetworkStringRes.ERR_SERVER),
                statusCode = code,
                body = body,
                endpoint = endpoint,
                requestId = requestId,
                headers = headers
            )
            else -> AppError.Unknown(strings.get(NetworkStringRes.ERR_UNKNOWN), endpoint = endpoint)
        }
    }
}

private fun Headers.asStringMap(): Map<String, String> =
    entries().associate { (key, values) -> key to values.joinToString(separator = ",") }

private fun Map<String, String>.requestId(): String? =
    entries.firstOrNull { (key, _) -> key.equals(NetHeaders.X_REQUEST_ID, ignoreCase = true) }?.value