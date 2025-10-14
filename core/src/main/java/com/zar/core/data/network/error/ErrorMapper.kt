package com.zar.core.data.network.error


import com.msa.finhub.R
import com.zar.core.data.network.network.common.StringProvider
import com.zar.core.data.network.network.handler.NetworkException

import io.ktor.client.plugins.*
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException


fun Throwable.toAppError(strings: StringProvider): AppError = when (this) {
    is NetworkException -> when (errorCode) {
        NetworkException.NetworkErrorCode.NETWORK_UNAVAILABLE ->
            ConnectionError("network_unavailable", strings.get(R.string.error_no_connection), connectionType)
        NetworkException.NetworkErrorCode.TIMEOUT ->
            TimeoutError("request_timeout", strings.get(R.string.error_timeout))
        NetworkException.NetworkErrorCode.SERVER_ERROR,
        NetworkException.NetworkErrorCode.SERVER_GENERIC ->
            ApiError(errorCode.code.toString(), strings.get(R.string.error_server), statusCode = errorCode.code)
        NetworkException.NetworkErrorCode.BAD_REQUEST,
        NetworkException.NetworkErrorCode.UNAUTHORIZED,
        NetworkException.NetworkErrorCode.FORBIDDEN,
        NetworkException.NetworkErrorCode.NOT_FOUND,
        NetworkException.NetworkErrorCode.CONFLICT,
        NetworkException.NetworkErrorCode.UNPROCESSABLE,
        NetworkException.NetworkErrorCode.LARGE_DOWNLOAD ->
            ApiError(errorCode.code.toString(), message, statusCode = errorCode.code)
        else -> UnknownError(message = strings.get(R.string.error_unknown))
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