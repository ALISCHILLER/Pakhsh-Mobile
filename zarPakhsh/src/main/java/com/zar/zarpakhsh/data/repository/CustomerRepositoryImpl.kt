package com.zar.zarpakhsh.data.repository


import com.zar.core.base.BaseRepository
import com.zar.core.data.network.result.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.zarpakhsh.data.local.dao.CustomerDao
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class CustomerRepositoryImpl(
    override val networkHandler: NetworkHandler,
    private val customerDao: CustomerDao
) : BaseRepository(networkHandler), CustomerRepository {
    override suspend fun CustomersList(): Flow<NetworkResult<List<CustomerModelEntity>>> {
        return getAsFlow<List<CustomerModelEntity>>(ApiEndpoints.CUSTOMER_LIST)
            .map { result ->
                result.map { responses ->
                    customerDao.refreshCustomers(responses)
                    responses
                }
            }
    }

    override suspend fun CustomerShiptoparty(): Flow<NetworkResult<List<CustomerModelEntity>>> {
        TODO("Not yet implemented")
    }
}


