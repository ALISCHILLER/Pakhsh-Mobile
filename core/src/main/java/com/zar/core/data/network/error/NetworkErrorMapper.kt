package com.zar.core.data.network.error


import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException


import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeoutException as JavaTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Maps low level networking exceptions to domain specific [AppError] instances. */
class NetworkErrorMapper {

    fun noConnection(): AppError = AppError.Network("No network connection", isTimeout = false)

    fun map(throwable: Throwable): AppError {
        if (throwable is CancellationException) throw throwable

        return when (throwable) {
            is TimeoutCancellationException,
            is HttpRequestTimeoutException,
            is JavaTimeoutException -> AppError.Network("Timeout", isTimeout = true)

            is SerializationException -> AppError.Serialization("Serialization error")

            is IOException -> AppError.Network(
                throwable.localizedMessage ?: "Network error",
                isTimeout = false,
            )

            is ClientRequestException -> mapStatus(throwable.response.status)
            is RedirectResponseException -> mapStatus(throwable.response.status)
            is ServerResponseException -> mapStatus(throwable.response.status)
            is ResponseException -> mapStatus(throwable.response.status)

            else -> {
                Timber.w(throwable, "Unhandled network exception")
                AppError.Unknown(throwable.message ?: "Unknown error")
            }
        }
    }

    private fun mapStatus(status: HttpStatusCode): AppError = when (status) {
        HttpStatusCode.Unauthorized -> AppError.Unauthorized()
        HttpStatusCode.Forbidden -> AppError.Forbidden()
        HttpStatusCode.NotFound -> AppError.NotFound()
        in HttpStatusCode.InternalServerError..HttpStatusCode.NetworkAuthenticationRequired -> AppError.Server()
        else -> AppError.Http("HTTP ${status.value}", status.value)
    }
}