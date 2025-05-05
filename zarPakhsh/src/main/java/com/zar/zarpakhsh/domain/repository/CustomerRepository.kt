package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer

interface CustomerRepository {

    suspend fun CustomersList(): NetworkResult<List<Customer>>
    suspend fun CustomerShiptoparty(): NetworkResult<List<Customer>>

}