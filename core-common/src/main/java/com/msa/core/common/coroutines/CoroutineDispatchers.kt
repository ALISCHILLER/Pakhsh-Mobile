package com.msa.core.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Shared dispatcher abstraction that keeps coroutine usage consistent across core modules.
 */
interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
    val unconfined: CoroutineDispatcher

    /**
     * Creates a new [CoroutineScope] bound to [SupervisorJob] on [parent].
     */
    fun newScope(parent: CoroutineContext = SupervisorJob()): CoroutineScope =
        CoroutineScope(parent + computation)

    suspend fun <T> withIo(block: suspend () -> T): T = withContext(io) { block() }

    suspend fun <T> withComputation(block: suspend () -> T): T = withContext(computation) { block() }
}

class DefaultCoroutineDispatchers(
    override val io: CoroutineDispatcher = Dispatchers.IO,
    override val computation: CoroutineDispatcher = Dispatchers.Default,
    override val main: CoroutineDispatcher = Dispatchers.Main,
    override val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate,
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined,
) : CoroutineDispatchers

class TestCoroutineDispatchers(private val dispatcher: CoroutineDispatcher) : CoroutineDispatchers {
    override val io: CoroutineDispatcher = dispatcher
    override val computation: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher

    override fun newScope(parent: CoroutineContext): CoroutineScope =
        CoroutineScope(parent + dispatcher)
}