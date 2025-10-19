package com.msa.core.network.circuit

import com.msa.core.network.config.CircuitPolicy
import java.util.ArrayDeque

class CircuitBreaker(
    private val policy: CircuitPolicy,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private enum class State { CLOSED, OPEN, HALF_OPEN }

    private var state: State = State.CLOSED
    private var openedAt: Long? = null
    private var halfOpenProbeInFlight: Boolean = false
    private val failureTimestamps = ArrayDeque<Long>()

    fun allow(): Boolean {
        if (!policy.enabled) return true
        val now = clock()
        purgeOldFailures(now)
        return when (state) {
            State.CLOSED -> true
            State.OPEN -> {
                val openTime = openedAt ?: now
                if (now - openTime >= policy.halfOpenAfterMs) {
                    state = State.HALF_OPEN
                    halfOpenProbeInFlight = false
                    allow()
                } else {
                    false
                }
            }
            State.HALF_OPEN ->
                if (!halfOpenProbeInFlight) {
                    halfOpenProbeInFlight = true
                    true
                } else {
                    false
                }
        }
    }

    fun onSuccess() {
        if (!policy.enabled) return
        when (state) {
            State.CLOSED -> {
                failureTimestamps.clear()
            }
            State.HALF_OPEN, State.OPEN -> {
                state = State.CLOSED
                openedAt = null
                halfOpenProbeInFlight = false
                failureTimestamps.clear()
            }
        }
    }

    fun onFailure() {
        if (!policy.enabled) return
        val now = clock()
        when (state) {
            State.HALF_OPEN -> {
                state = State.OPEN
                openedAt = now
                halfOpenProbeInFlight = false
                failureTimestamps.clear()
            }
            State.OPEN -> {
                openedAt = openedAt ?: now
            }
            State.CLOSED -> {
                failureTimestamps.addLast(now)
                purgeOldFailures(now)
                if (failureTimestamps.size >= policy.failureThreshold) {
                    state = State.OPEN
                    openedAt = now
                    halfOpenProbeInFlight = false
                    failureTimestamps.clear()
                }
            }
        }
    }

    private fun purgeOldFailures(now: Long) {
        if (policy.rollingWindowMs <= 0) return
        while (failureTimestamps.isNotEmpty() && now - failureTimestamps.peekFirst() > policy.rollingWindowMs) {
            failureTimestamps.removeFirst()
        }
    }
}