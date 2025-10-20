package com.zar.core.data.network.error

import com.msa.core.common.error.AppError
import com.zar.core.R
import com.zar.core.data.network.common.StringProvider
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException


class ErrorMapper(private val strings: StringProvider) {

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

            is RedirectResponseException -> fromStatusCode(throwable.response.status.value)
            is ClientRequestException -> mapClientError(throwable)
            is ServerResponseException -> fromStatusCode(throwable.response.status.value)
            is ResponseException -> fromStatusCode(throwable.response.status.value)
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

    fun fromStatusCode(statusCode: Int, fallbackMessage: String? = null): AppError {
        val msg = fallbackMessage?.takeIf { it.isNotBlank() }
        return when (statusCode) {
            HttpStatusCode.BadRequest.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_bad_request),
                statusCode = statusCode,
                errorCode = "bad_request",
                endpoint = ""
            )

            HttpStatusCode.Unauthorized.value -> AppError.Auth(
                message = msg ?: strings.get(R.string.error_unauthorized),
                reason = "HTTP_$statusCode"
            )

            HttpStatusCode.Forbidden.value -> AppError.Auth(
                message = msg ?: strings.get(R.string.error_forbidden),
                reason = "HTTP_$statusCode"
            )

            HttpStatusCode.NotFound.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_not_found),
                statusCode = statusCode,
                errorCode = "not_found",
                endpoint = ""
            )

            HttpStatusCode.Conflict.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_conflict),
                statusCode = statusCode,
                errorCode = "conflict",
                endpoint = ""
            )

            HttpStatusCode.PayloadTooLarge.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_large_download),
                statusCode = statusCode,
                errorCode = "payload_too_large",
                endpoint = ""
            )

            HttpStatusCode.UnprocessableEntity.value -> AppError.Server(
                message = msg ?: strings.get(R.string.error_unprocessable),
                statusCode = statusCode,
                errorCode = "unprocessable_entity",
                endpoint = ""
            )

            in 300..399 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_redirect, statusCode),
                statusCode = statusCode,
                errorCode = "redirect",
                endpoint = ""
            )

            in 400..499 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_client_generic, statusCode),
                statusCode = statusCode,
                errorCode = "client_error",
                endpoint = ""
            )

            in 500..599 -> AppError.Server(
                message = msg ?: strings.get(R.string.error_server_code, statusCode),
                statusCode = statusCode,
                errorCode = "server_error",
                endpoint = ""
            )

            else -> AppError.Server(
                message = msg ?: strings.get(R.string.error_server_generic),
                statusCode = statusCode,
                errorCode = statusCode.toString(),
                endpoint = ""
            )
        }
    }

    private fun mapClientError(exception: ClientRequestException): AppError {
        val status = exception.response.status
        val specific = when (status) {
            HttpStatusCode.BadRequest -> strings.get(R.string.error_bad_request)
            HttpStatusCode.Unauthorized -> strings.get(R.string.error_unauthorized)
            HttpStatusCode.Forbidden -> strings.get(R.string.error_forbidden)
            HttpStatusCode.NotFound -> strings.get(R.string.error_not_found)
            HttpStatusCode.Conflict -> strings.get(R.string.error_conflict)
            HttpStatusCode.PayloadTooLarge -> strings.get(R.string.error_large_download)
            HttpStatusCode.UnprocessableEntity -> strings.get(R.string.error_unprocessable)
            else -> null
        }
        val message = specific ?: strings.get(R.string.error_client_generic, status.value)
        return fromStatusCode(status.value, message)
    }
}

fun Throwable.toAppError(strings: StringProvider): AppError =
    ErrorMapper(strings).fromThrowable(this)
