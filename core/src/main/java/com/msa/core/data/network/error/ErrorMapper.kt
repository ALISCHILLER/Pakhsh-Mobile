package com.msa.core.data.network.error


import com.msa.core.R
import com.msa.core.common.error.AppError
import com.msa.core.data.network.common.StringProvider
import com.msa.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ErrorMapper(
    private val strings: StringProvider,
    private val clock: Clock = Clock.systemUTC()
) {

    fun fromThrowable(throwable: Throwable): AppError {
        if (throwable is CancellationException) throw throwable

        return when (throwable) {
            is HttpRequestTimeoutException,
            is SocketTimeoutException -> AppError.Timeout(
                message = strings.get(R.string.error_timeout),
                errorCode = "request_timeout",
                cause = throwable
            )

            is UnknownHostException,
            is ConnectException -> AppError.Network(
                message = strings.get(R.string.error_no_connection),
                errorCode = "dns_or_connect_error",
                isConnectivity = true
            )

            is SSLException -> AppError.Network(
                message = strings.get(R.string.error_unknown),
                errorCode = "ssl_error",
                isConnectivity = true
            )

            is RedirectResponseException -> throwable.toStatusError()
            is ClientRequestException -> mapClientError(throwable)
            is ServerResponseException -> throwable.toStatusError()
            is ResponseException -> throwable.toStatusError()
            is SerializationException -> AppError.Parsing(
                message = strings.get(R.string.error_parsing),
                errorCode = "serialization_error"
            )

            else -> AppError.Unknown(
                message = strings.get(R.string.error_unknown),
                errorCode = "unknown_error",
                cause = throwable
            )
        }
    }

    fun noConnection(connectionType: NetworkStatusMonitor.ConnectionType?): AppError.Network =
        AppError.Network(
            message = strings.get(R.string.error_no_connection),
            errorCode = "network_unavailable",
            isConnectivity = true,
            connectionType = connectionType?.name
        )

    fun emptyBody(): AppError.Parsing = AppError.Parsing(
        message = strings.get(R.string.error_parsing),
        errorCode = "empty_response_body"
    )

    fun fromStatusCode(
        statusCode: Int,
        fallbackMessage: String? = null,
        headers: Headers? = null,
        endpoint: String = ""
    ): AppError {
        val msg = fallbackMessage?.takeIf { it.isNotBlank() }
        return when (statusCode) {
            HttpStatusCode.BadRequest.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_bad_request),
                statusCode = statusCode,
                errorCode = "bad_request",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            HttpStatusCode.Unauthorized.value -> AppError.Auth(
                message = msg ?: strings.get(R.string.error_unauthorized),
                reason = "HTTP_$statusCode",
                endpoint = endpoint.takeIf { it.isNotBlank() }
            )

            HttpStatusCode.Forbidden.value -> AppError.Auth(
                message = msg ?: strings.get(R.string.error_forbidden),
                reason = "HTTP_$statusCode",
                endpoint = endpoint.takeIf { it.isNotBlank() }
            )

            HttpStatusCode.NotFound.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_not_found),
                statusCode = statusCode,
                endpoint = endpoint,
                headers = headers.asMap()
            )

            HttpStatusCode.Conflict.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_conflict),
                statusCode = statusCode,
                endpoint = endpoint,
                headers = headers.asMap()
            )

            HttpStatusCode.PayloadTooLarge.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_large_download),
                statusCode = statusCode,
                errorCode = "payload_too_large",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            HttpStatusCode.UnprocessableEntity.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_unprocessable),
                statusCode = statusCode,
                errorCode = "unprocessable_entity",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            HttpStatusCode.TooManyRequests.value -> {
                val retryInfo = extractRetryAfter(headers)
                val finalMessage = when {
                    msg != null -> msg
                    retryInfo.seconds != null ->
                        strings.get(R.string.error_too_many_requests_retry, retryInfo.seconds)
                    else -> strings.get(R.string.error_too_many_requests)
                }
                AppError.RateLimited(
                    message = finalMessage,
                    statusCode = statusCode,
                    retryAfterSeconds = retryInfo.seconds,
                    retryAfterRaw = retryInfo.raw,
                    endpoint = endpoint.takeIf { it.isNotBlank() },
                    headers = headers.asMap()
                )
            }

            in 300..399 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_redirect, statusCode),
                statusCode = statusCode,
                errorCode = "redirect",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            in 400..499 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_client_generic, statusCode),
                statusCode = statusCode,
                errorCode = "client_error",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            in 500..599 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_server_code, statusCode),
                statusCode = statusCode,
                errorCode = "server_error",
                endpoint = endpoint,
                headers = headers.asMap()
            )

            else -> AppError.Server(
                message = msg ?: strings.get(R.string.error_server_generic),
                statusCode = statusCode,
                errorCode = statusCode.toString(),
                endpoint = endpoint,
                headers = headers.asMap()
            )
        }
    }

    private fun mapClientError(exception: ClientRequestException): AppError {
        val status = exception.response.status
        val headers = exception.response.headers
        val endpoint = exception.response.request.url.toString()

        val specific = when (status) {
            HttpStatusCode.BadRequest -> strings.get(R.string.error_bad_request)
            HttpStatusCode.Unauthorized -> strings.get(R.string.error_unauthorized)
            HttpStatusCode.Forbidden -> strings.get(R.string.error_forbidden)
            HttpStatusCode.NotFound -> strings.get(R.string.error_not_found)
            HttpStatusCode.Conflict -> strings.get(R.string.error_conflict)
            HttpStatusCode.PayloadTooLarge -> strings.get(R.string.error_large_download)
            HttpStatusCode.UnprocessableEntity -> strings.get(R.string.error_unprocessable)
            HttpStatusCode.TooManyRequests -> null
            else -> null
        }
        val fallback = when {
            specific != null -> specific
            status == HttpStatusCode.TooManyRequests -> null
            else -> strings.get(R.string.error_client_generic, status.value)
        }
        return fromStatusCode(
            statusCode = status.value,
            fallbackMessage = fallback,
            headers = headers,
            endpoint = endpoint
        )
    }

    private fun ResponseException.toStatusError(): AppError {
        val status = response.status
        val fallback = status.description.takeUnless { status == HttpStatusCode.TooManyRequests }
        return fromStatusCode(
            statusCode = status.value,
            fallbackMessage = fallback,
            headers = response.headers,
            endpoint = response.request.url.toString()
        )
    }

    private fun extractRetryAfter(headers: Headers?): RetryAfterInfo {
        val raw = headers?.get(HttpHeaders.RetryAfter)?.trim()?.takeIf { it.isNotEmpty() }
            ?: return RetryAfterInfo(null, null)
        val seconds = raw.toLongOrNull() ?: run {
            runCatching {
                val targetInstant = ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
                val duration = Duration.between(clock.instant(), targetInstant)
                if (!duration.isNegative) duration.seconds else 0L
            }.getOrNull()
        }
        val normalized = seconds?.coerceAtLeast(0)
        return RetryAfterInfo(normalized, raw)
    }
    private fun Headers?.asMap(): Map<String, String> =
        this?.entries()?.associate { entry ->
            entry.key to entry.value.joinToString(separator = ",")
        } ?: emptyMap()

    private data class RetryAfterInfo(val seconds: Long?, val raw: String?)
}

fun Throwable.toAppError(strings: StringProvider): AppError =
    ErrorMapper(strings).fromThrowable(this)
