package com.msa.core.common.api

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents the canonical result wrapper that is shared across all core modules.
 */
sealed class Outcome<out T> {
    data class Success<T>(val value: T, val meta: Meta = Meta.EMPTY) : Outcome<T>()
    data class Failure(val error: AppError) : Outcome<Nothing>()

    inline fun onSuccess(block: (T, Meta) -> Unit): Outcome<T> = also {
        if (this is Success) {
            block(value, meta)
        }
    }

    inline fun onFailure(block: (AppError) -> Unit): Outcome<T> = also {
        if (this is Failure) {
            block(error)
        }
    }

    fun getOrNull(): T? = (this as? Success)?.value
    fun errorOrNull(): AppError? = (this as? Failure)?.error
}

/**
 * Represents metadata that can be attached to a successful outcome.
 */
data class Meta(
    val statusCode: Int? = null,
    val message: String? = null,
    val pagination: Pagination? = null,
    val extras: Map<String, String> = emptyMap()
) {
    fun merge(other: Meta): Meta = Meta(
        statusCode = other.statusCode ?: statusCode,
        message = other.message ?: message,
        pagination = other.pagination ?: pagination,
        extras = extras + other.extras
    )

    companion object {
        val EMPTY = Meta()
    }
}

/**
 * Lightweight pagination descriptor.
 */
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val totalItems: Long? = null,
    val totalPages: Int? = null
)

/**
 * Canonical application level error mapping. The hierarchy is intentionally minimal
 * to allow simple error handling in higher layers.
 */
sealed interface AppError {
    val message: String

    data class Network(override val message: String, val cause: Throwable? = null) : AppError
    data class Server(override val message: String, val statusCode: Int? = null) : AppError
    data class Authentication(override val message: String) : AppError
    data class Validation(override val message: String, val issues: Map<String, String> = emptyMap()) : AppError
    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError
}

/**
 * Convenience extension for building a failure outcome.
 */
fun AppError.asFailure(): Outcome.Failure = Outcome.Failure(this)

/**
 * Convenience for creating an outcome from a block.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> outcomeOf(block: () -> T): Outcome<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return runCatching(block)
        .fold(
            onSuccess = { Outcome.Success(it) },
            onFailure = { throwable ->
                Outcome.Failure(
                    when (throwable) {
                        is AppError.Network -> throwable
                        is AppError.Server -> throwable
                        is AppError.Authentication -> throwable
                        is AppError.Validation -> throwable
                        else -> AppError.Unknown(
                            message = throwable.message ?: "Unknown error",
                            cause = throwable
                        )
                    }
                )
            }
        )
}