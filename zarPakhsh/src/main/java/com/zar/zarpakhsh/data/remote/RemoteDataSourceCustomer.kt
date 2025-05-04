package com.zar.zarpakhsh.data.remote

import com.zar.core.base.BaseRepository
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.data.models.CustomerResponse



class RemoteDataSourceCustomer(
    networkHandler: NetworkHandler
) : BaseRepository(networkHandler) {

    suspend fun getCustomerAddressList(): NetworkResult<List<CustomerResponse>> {
        return safeGetRequest(ApiEndpoints.CUSTOMER_LIST)
    }
}