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


    is HttpRequestTimeoutException, is SocketTimeoutException ->
        TimeoutError("request_timeout", strings.get(R.string.error_timeout))


    is UnknownHostException, is ConnectException ->
        ConnectionError("dns_or_connect_error", strings.get(R.string.error_no_connection))


    is RedirectResponseException ->
        ApiError(response.status.value.toString(), response.status.description, response.status.value)


    is ClientRequestException ->
        ApiError(response.status.value.toString(), response.status.description, response.status.value)


    is ServerResponseException ->
        ApiError(response.status.value.toString(), response.status.description, response.status.value)


    is SSLException -> ConnectionError("ssl_error", strings.get(R.string.error_unknown))


    is SerializationException -> ParsingError("serialization_error", strings.get(R.string.error_server_generic))


    else -> UnknownError(message = strings.get(R.string.error_unknown))
}