package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {

    suspend fun CustomersList(): Flow<NetworkResult<List<CustomerModelEntity>>>
    suspend fun CustomerShiptoparty(): Flow<NetworkResult<List<CustomerModelEntity>>>

}