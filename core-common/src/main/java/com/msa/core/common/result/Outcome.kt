package com.msa.core.common.result

import com.msa.core.common.error.AppError
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract



sealed class Outcome<out V> {
    data class Success<V>(val value: V, val meta: Meta = Meta()) : Outcome<V>()
    data class Failure(val error: AppError) : Outcome<Nothing>()
}

inline fun <V, R> Outcome<V>.map(transform: (V) -> R): Outcome<R> =
    when (this) {
        is Outcome.Success -> Outcome.Success(transform(value), meta)
        is Outcome.Failure -> this
    }

inline fun <V> Outcome<V>.onSuccess(block: (value: V, meta: Meta) -> Unit): Outcome<V> =
    apply { if (this is Outcome.Success) block(value, meta) }

inline fun <V> Outcome<V>.onFailure(block: (AppError) -> Unit): Outcome<V> =
    apply { if (this is Outcome.Failure) block(error) }

fun <V> Outcome<V>.getOrNull(): V? = (this as? Outcome.Success)?.value

fun <V> Outcome<V>.errorOrNull(): AppError? = (this as? Outcome.Failure)?.error

@Deprecated("Use onSuccess", ReplaceWith("onSuccess(block)"))
inline fun <V> Outcome<V>.onOk(block: (value: V, meta: Meta) -> Unit): Outcome<V> = onSuccess(block)

@Deprecated("Use onFailure", ReplaceWith("onFailure(block)"))
inline fun <V> Outcome<V>.onFail(block: (AppError) -> Unit): Outcome<V> = onFailure(block)

fun AppError.asFailure(): Outcome.Failure = Outcome.Failure(this)

@OptIn(ExperimentalContracts::class)
inline fun <T> outcomeOf(block: () -> T): Outcome<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return runCatching(block).fold(
        onSuccess = { Outcome.Success(it) },
        onFailure = { throwable ->
            when (throwable) {
                is AppError -> Outcome.Failure(throwable)
                else -> Outcome.Failure(
                    AppError.Unknown(
                        message = throwable.message,
                        cause = throwable
                    )
                )
            }
        }
    )
}