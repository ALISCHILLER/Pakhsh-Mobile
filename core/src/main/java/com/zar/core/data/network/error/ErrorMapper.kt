package com.zar.core.data.network.error


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
            is SocketTimeoutException -> TimeoutError(
                errorCode = "request_timeout",
                message = strings.get(R.string.error_timeout)
            )

            is UnknownHostException,
            is ConnectException -> ConnectionError(
                errorCode = "dns_or_connect_error",
                message = strings.get(R.string.error_no_connection)
            )

            is SSLException -> ConnectionError(
                errorCode = "ssl_error",
                message = strings.get(R.string.error_unknown)
            )

            is RedirectResponseException -> fromStatusCode(throwable.response.status.value)
            is ClientRequestException -> mapClientError(throwable)
            is ServerResponseException -> fromStatusCode(throwable.response.status.value)
            is ResponseException -> fromStatusCode(throwable.response.status.value)
            is SerializationException -> ParsingError(
                errorCode = "serialization_error",
                message = strings.get(R.string.error_parsing)
            )

            else -> UnknownError(
                message = strings.get(R.string.error_unknown)
            )
        }
    }
    fun noConnection(connectionType: NetworkStatusMonitor.ConnectionType?): ConnectionError = ConnectionError(
        errorCode = "network_unavailable",
        message = strings.get(R.string.error_no_connection),
        connectionType = connectionType?.name
    )

    fun emptyBody(): ParsingError = ParsingError(
        errorCode = "empty_response_body",
        message = strings.get(R.string.error_parsing)
    )

    fun fromStatusCode(statusCode: Int, fallbackMessage: String? = null): AppError {
        val msg = fallbackMessage?.takeIf { it.isNotBlank() }
        return when (statusCode) {
            HttpStatusCode.BadRequest.value -> ApiError(
                errorCode = "bad_request",
                message = strings.get(R.string.error_bad_request),
                statusCode = statusCode
            )

            HttpStatusCode.Unauthorized.value -> ApiError(
                errorCode = "unauthorized",
                message = strings.get(R.string.error_unauthorized),
                statusCode = statusCode
            )

            HttpStatusCode.Forbidden.value -> ApiError(
                errorCode = "forbidden",
                message = strings.get(R.string.error_forbidden),
                statusCode = statusCode
            )

            HttpStatusCode.NotFound.value -> ApiError(
                errorCode = "not_found",
                message = strings.get(R.string.error_not_found),
                statusCode = statusCode
            )

            HttpStatusCode.Conflict.value -> ApiError(
                errorCode = "conflict",
                message = strings.get(R.string.error_conflict),
                statusCode = statusCode
            )

            HttpStatusCode.PayloadTooLarge.value -> ApiError(
                errorCode = "payload_too_large",
                message = strings.get(R.string.error_large_download),
                statusCode = statusCode
            )

            HttpStatusCode.UnprocessableEntity.value -> ApiError(
                errorCode = "unprocessable_entity",
                message = strings.get(R.string.error_unprocessable),
                statusCode = statusCode
            )

            in 300..399 -> ApiError(
                errorCode = "redirect",
                message = strings.get(R.string.error_redirect, statusCode),
                statusCode = statusCode
            )

            in 400..499 -> ApiError(
                errorCode = "client_error",
                message = strings.get(R.string.error_client_generic, statusCode),
                statusCode = statusCode
            )

            in 500..599 -> ApiError(
                errorCode = "server_error",
                message = strings.get(R.string.error_server_code, statusCode),
                statusCode = statusCode
            )

            else -> ApiError(
                errorCode = statusCode.toString(),
                message = msg ?: strings.get(R.string.error_server_generic),
                statusCode = statusCode
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
        return ApiError(
            errorCode = status.value.toString(),
            message = message,
            statusCode = status.value
        )
    }
}

/** برای سازگاری با NetworkResult.Error.fromException */
fun Throwable.toAppError(strings: StringProvider): AppError =
    ErrorMapper(strings).fromThrowable(this)
