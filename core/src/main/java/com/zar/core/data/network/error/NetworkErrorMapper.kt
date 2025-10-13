package com.zar.core.data.network.error

import android.content.Context
import com.zar.core.R
import com.zar.core.data.network.utils.NetworkStatusMonitor
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.TimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Maps thrown exceptions from the network stack into strongly typed [AppError] models that can be
 * consumed by higher layers.
 */
class NetworkErrorMapper(private val context: Context) {

    fun noConnection(connectionType: NetworkStatusMonitor.ConnectionType?): ConnectionError {
        return ConnectionError(
            errorCode = "network_unavailable",
            message = context.getString(R.string.error_no_connection),
            connectionType = connectionType?.name,
        )
    }

    fun map(throwable: Throwable): AppError {
        if (throwable is CancellationException) throw throwable

        return when (throwable) {
            is TimeoutCancellationException, is TimeoutException -> {
                TimeoutError(
                    errorCode = "request_timeout",
                    message = context.getString(R.string.error_timeout),
                    duration = 30_000,
                    cause = throwable,
                )
            }

            is SerializationException -> {
                ParsingError(
                    errorCode = "parsing_error",
                    message = context.getString(R.string.error_parsing),
                )
            }

            is IOException -> {
                ConnectionError(
                    errorCode = "io_exception",
                    message = throwable.localizedMessage
                        ?: context.getString(R.string.error_no_connection),
                    connectionType = null,
                )
            }

            is ResponseException -> mapResponseException(throwable)

            else -> {
                Timber.e(throwable, "Unhandled exception in network call")
                UnknownError(message = context.getString(R.string.error_unknown))
            }
        }
    }

    private fun mapResponseException(exception: ResponseException): AppError {
        val status: HttpStatusCode = exception.response.status
        return when (exception) {
            is ClientRequestException -> mapClientException(exception, status)
            is RedirectResponseException -> mapRedirectException(status)
            is ServerResponseException -> mapServerException(status)
            else -> mapGenericResponseException(exception, status)
        }
    }

    private fun mapClientException(
        exception: ClientRequestException,
        status: HttpStatusCode,
    ): AppError {
        val message = exception.message?.takeIf { it.isNotBlank() }
            ?: clientErrorMessage(status)

        return ApiError(
            errorCode = status.value.toString(),
            message = message,
            statusCode = status.value,
        )
    }

    private fun mapRedirectException(status: HttpStatusCode): AppError {
        val message = context.getString(R.string.error_redirect, status.value)
        return ApiError(
            errorCode = status.value.toString(),
            message = message,
            statusCode = status.value,
        )
    }

    private fun mapServerException(status: HttpStatusCode): AppError = serverError(status.value)

    private fun mapGenericResponseException(
        exception: ResponseException,
        status: HttpStatusCode,
    ): AppError {
        val message = exception.message?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.error_unknown)

        return ApiError(
            errorCode = status.value.toString(),
            message = message,
            statusCode = status.value,
        )
    }

    private fun clientErrorMessage(status: HttpStatusCode): String {
        return when (status) {
            HttpStatusCode.BadRequest -> context.getString(R.string.error_bad_request)
            HttpStatusCode.Unauthorized -> context.getString(R.string.error_unauthorized)
            HttpStatusCode.Forbidden -> context.getString(R.string.error_forbidden)
            HttpStatusCode.NotFound -> context.getString(R.string.error_not_found)
            HttpStatusCode.Conflict -> context.getString(R.string.error_conflict)
            HttpStatusCode.UnprocessableEntity -> context.getString(R.string.error_unprocessable)
            else -> context.getString(R.string.error_client_generic, status.value)
        }
    }

    fun serverError(code: Int, fallbackMessage: String? = null): ApiError {
        val message = fallbackMessage?.takeIf { it.isNotBlank() }
            ?: if (code in 500..599) {
                context.getString(R.string.error_server_code, code)
            } else {
                context.getString(R.string.error_server_generic)
            }

        return ApiError(
            errorCode = code.toString(),
            message = message,
            statusCode = code,
        )
    }
}