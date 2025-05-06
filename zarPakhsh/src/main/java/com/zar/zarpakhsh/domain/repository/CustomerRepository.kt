package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.entities.User
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {

    suspend fun CustomersList(): Flow<NetworkResult<List<Customer>>>
    suspend fun CustomerShiptoparty(): Flow<NetworkResult<List<Customer>>>

}