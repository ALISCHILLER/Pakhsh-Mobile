package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.entities.Product
import com.zar.zarpakhsh.domain.entities.Settings

interface TourRepository {
    suspend fun getCustomerList(): NetworkResult<List<Customer>>
    suspend fun getProductList(): NetworkResult<List<Product>>
    suspend fun getSettings(): NetworkResult<Settings>

}