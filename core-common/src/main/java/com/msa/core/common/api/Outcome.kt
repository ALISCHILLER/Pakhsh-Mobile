@file:Suppress("DEPRECATION")
package com.msa.core.common.api

import com.msa.core.common.error.AppError as CoreAppError
import com.msa.core.common.result.Meta as ResultMeta
import com.msa.core.common.result.Outcome as ResultOutcome
import com.msa.core.common.result.asFailure as resultAsFailure
import com.msa.core.common.result.outcomeOf as resultOutcomeOf
import com.msa.core.common.paging.PageInfo

@Deprecated(
    message = "Use com.msa.core.common.result.Outcome",
    replaceWith = ReplaceWith("Outcome<T>", "com.msa.core.common.result.Outcome")
)
typealias Outcome<T> = ResultOutcome<T>

@Deprecated(
    message = "Use com.msa.core.common.result.Meta",
    replaceWith = ReplaceWith("Meta", "com.msa.core.common.result.Meta")
)

typealias Meta = ResultMeta
@Deprecated(
    message = "Use com.msa.core.common.paging.PageInfo",
    replaceWith = ReplaceWith("PageInfo", "com.msa.core.common.paging.PageInfo")
)
typealias Pagination = PageInfo

@Deprecated(
    message = "Use com.msa.core.common.error.AppError",
    replaceWith = ReplaceWith("AppError", "com.msa.core.common.error.AppError")
)
typealias AppError = CoreAppError

@Deprecated(
    message = "Use com.msa.core.common.result.asFailure",
    replaceWith = ReplaceWith("this.asFailure()", "com.msa.core.common.result.asFailure")
)
fun CoreAppError.asFailure(): ResultOutcome.Failure = this.resultAsFailure()

@Deprecated(
    message = "Use com.msa.core.common.result.outcomeOf",
    replaceWith = ReplaceWith("outcomeOf(block)", "com.msa.core.common.result.outcomeOf")
)
inline fun <T> outcomeOf(noinline block: () -> T): ResultOutcome<T> = resultOutcomeOf(block)