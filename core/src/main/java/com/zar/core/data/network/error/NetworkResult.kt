package com.zar.core.data.network.error



sealed class NetworkResult<out T> {


    object Loading : NetworkResult<Nothing>()


    object Idle : NetworkResult<Nothing>()

    data class Success<out T>(
        val data: T,
        val metadata: NetworkMetadata = NetworkMetadata(),
    ) : NetworkResult<T>()


    data class Error(
        val error: AppError,
        val cause: Throwable? = null,
        val metadata: NetworkMetadata = NetworkMetadata(),
    ) : NetworkResult<Nothing>()
}

data class NetworkMetadata(
    val statusCode: Int? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val requestMethod: String? = null,
    val requestUrl: String? = null,
    val requestLabel: String? = null,
    val connectionType: String? = null,
    val receivedAtEpochMillis: Long = System.currentTimeMillis(),
)


// Extension Functions
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data), metadata)
        is NetworkResult.Error -> this
        is NetworkResult.Loading -> this
        is NetworkResult.Idle -> this
    }
}

inline fun <T> NetworkResult<T>.mapError(transform: (AppError) -> AppError): NetworkResult<T> {
    return when (this) {
        is NetworkResult.Error -> copy(error = transform(error))
        else -> this
    }
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