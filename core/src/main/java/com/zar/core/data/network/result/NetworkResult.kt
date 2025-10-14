package com.zar.core.data.network.result

import com.zar.core.base.UIState
import com.zar.core.data.network.error.AppError

/**
 * Represents the lifecycle of a network request. Each result carries optional [NetworkMetadata]
 * which allows downstream layers to reason about the originating request and response without
 * needing to inspect raw Ktor primitives.
 */
sealed class NetworkResult<out T> {

    /** Indicates that a request is currently executing. */
    object Loading : NetworkResult<Nothing>()

    /** Idle state used when nothing has been requested yet. */
    object Idle : NetworkResult<Nothing>()

    /** Successful result carrying the parsed payload and optional [metadata]. */
    data class Success<out T>(
        val data: T,
        val metadata: NetworkMetadata = NetworkMetadata(),
    ) : NetworkResult<T>()

    /** Failure result with a typed [AppError], optional [cause] and [metadata]. */
    data class Error(
        val error: AppError,
        val cause: Throwable? = null,
        val metadata: NetworkMetadata = NetworkMetadata(),
    ) : NetworkResult<Nothing>()
}

/**
 * Supplemental metadata captured alongside each network response. This is useful for logging,
 * debugging and surfacing contextual information in the UI layer.
 */
data class NetworkMetadata(
    val statusCode: Int? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val requestLabel: String? = null,
    val connectionType: String? = null,
    val method: String? = null,
    val url: String? = null,
    val traceId: String? = null,
    val receivedAtEpochMillis: Long = System.currentTimeMillis(),
)

// region Extension functions

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data), metadata)
    is NetworkResult.Error -> this
    NetworkResult.Loading -> NetworkResult.Loading
    NetworkResult.Idle -> NetworkResult.Idle
}

inline fun <T> NetworkResult<T>.mapError(transform: (AppError) -> AppError): NetworkResult<T> = when (this) {
    is NetworkResult.Error -> copy(error = transform(error))
    else -> this
}

inline fun <T> NetworkResult<T>.onSuccess(action: (T, NetworkMetadata) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data, metadata)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (AppError, NetworkMetadata) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(error, metadata)
    return this
}

fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action()
    return this
}

fun <T> NetworkResult<T>.onIdle(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Idle) action()
    return this
}

fun <T> NetworkResult<T>.successOrNull(): T? = (this as? NetworkResult.Success)?.data

fun <T> NetworkResult<T>.errorOrNull(): AppError? = (this as? NetworkResult.Error)?.error

fun <T> NetworkResult<T>.toUiState(): UIState<T> = when (this) {
    is NetworkResult.Success -> UIState.Success(data)
    is NetworkResult.Error -> UIState.Error(error, cause)
    NetworkResult.Loading -> UIState.Loading
    NetworkResult.Idle -> UIState.Idle
}

inline fun <T, R> NetworkResult<T>.fold(
    onSuccess: (T, NetworkMetadata) -> R,
    onError: (AppError, Throwable?, NetworkMetadata) -> R,
    onLoading: () -> R,
    onIdle: () -> R,
): R = when (this) {
    is NetworkResult.Success -> onSuccess(data, metadata)
    is NetworkResult.Error -> onError(error, cause, metadata)
    NetworkResult.Loading -> onLoading()
    NetworkResult.Idle -> onIdle()
}

inline fun <T> NetworkResult<T>.getOrElse(onError: (AppError) -> T): T = when (this) {
    is NetworkResult.Success -> data
    is NetworkResult.Error -> onError(error)
    NetworkResult.Loading, NetworkResult.Idle ->
        throw IllegalStateException("Value is not available when result is $this")
}

fun <T> NetworkResult<T>.requireSuccess(message: (() -> String)? = null): T = when (this) {
    is NetworkResult.Success -> data
    is NetworkResult.Error -> throw NetworkResultException(error, metadata, cause)
    NetworkResult.Loading, NetworkResult.Idle -> throw IllegalStateException(
        message?.invoke() ?: "Expected success but was $this",
    )
}

fun <T> NetworkResult<T>.toResult(): Result<T> = when (this) {
    is NetworkResult.Success -> Result.success(data)
    is NetworkResult.Error -> Result.failure(NetworkResultException(error, metadata, cause))
    NetworkResult.Loading -> Result.failure(IllegalStateException("Result is still loading"))
    NetworkResult.Idle -> Result.failure(IllegalStateException("Result is idle"))
}

inline fun <T> NetworkResult<T>.recover(transform: (AppError) -> T): NetworkResult<T> = when (this) {
    is NetworkResult.Error -> NetworkResult.Success(transform(error), metadata)
    else -> this
}

fun <T> NetworkResult<T>.isTerminal(): Boolean = this is NetworkResult.Success || this is NetworkResult.Error

// endregion

class NetworkResultException(
    val error: AppError,
    val metadata: NetworkMetadata,
    cause: Throwable? = null,
) : RuntimeException(error.message, cause)