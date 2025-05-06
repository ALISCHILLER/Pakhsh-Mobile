package com.zar.zarpakhsh.data.repository

import android.content.Context
import com.zar.core.base.BaseRepository
import com.zar.core.base.map
import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.zarpakhsh.data.local.dao.CustomerDao
import com.zar.zarpakhsh.data.mappers.toDomain
import com.zar.zarpakhsh.data.mappers.toEntity
import com.zar.zarpakhsh.data.models.CustomerResponse
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class CustomerRepositoryImpl(
    override val networkHandler: NetworkHandler
) : BaseRepository(networkHandler), CustomerRepository {
    override suspend fun CustomersList(): Flow<NetworkResult<List<Customer>>> {
        return getAsFlow<List<CustomerResponse>>(ApiEndpoints.CUSTOMER_LIST)
            .map { result ->
                result.map { responses ->
                    val domainList = responses.map { it.toDomain() }
                  //  customerDao.refreshCustomers(domainList.map { it.toEntity() })
                    domainList
                }
            }
    }

    override suspend fun CustomerShiptoparty(): Flow<NetworkResult<List<Customer>>> {
        TODO("Not yet implemented")
    }
}


