package com.msa.core.network.config

data class PolicySnapshot(
    val cachePolicy: CachePolicy,
    val retry: RetryPolicy,
    val circuit: CircuitPolicy
)