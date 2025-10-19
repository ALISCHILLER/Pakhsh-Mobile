package com.msa.core.common.result

import com.zar.core.common.error.AppError

sealed interface Outcome<out V> {
    data class Ok<V>(val value: V, val meta: Meta = Meta()) : Outcome<V>
    data class Fail(val error: AppError) : Outcome<Nothing>
}

inline fun <V, R> Outcome<V>.map(transform: (V) -> R): Outcome<R> =
    when (this) {
        is Outcome.Ok -> Outcome.Ok(transform(value), meta)
        is Outcome.Fail -> this
    }

inline fun <V> Outcome<V>.onOk(block: (value: V, meta: Meta) -> Unit): Outcome<V> =
    apply { if (this is Outcome.Ok) block(value, meta) }

inline fun <V> Outcome<V>.onFail(block: (AppError) -> Unit): Outcome<V> =
    apply { if (this is Outcome.Fail) block(error) }