package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.local.entity.ProductEntity
import com.zar.zarpakhsh.data.local.entity.ProductGroupEntity
import com.zar.zarpakhsh.data.local.entity.ProductUnitEntity
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    suspend fun getProducts (): Flow<NetworkResult<List<ProductEntity>>>
    suspend fun getProductGroups (): Flow<NetworkResult<List<ProductGroupEntity>>>
    suspend fun getProductUnits (): Flow<NetworkResult<List<ProductUnitEntity>>>

}