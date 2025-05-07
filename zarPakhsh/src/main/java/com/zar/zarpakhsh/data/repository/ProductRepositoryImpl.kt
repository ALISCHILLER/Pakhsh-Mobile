package com.zar.zarpakhsh.data.repository

import com.zar.core.base.BaseRepository
import com.zar.core.base.map
import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.zarpakhsh.data.local.dao.ProductDao
import com.zar.zarpakhsh.data.local.dao.ProductGroupDao
import com.zar.zarpakhsh.data.local.entity.ProductEntity
import com.zar.zarpakhsh.data.local.entity.ProductGroupEntity
import com.zar.zarpakhsh.data.local.entity.ProductUnitEntity
import com.zar.zarpakhsh.data.mappers.toDomain
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import com.zar.zarpakhsh.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    networkHandler: NetworkHandler,
    private val productDao: ProductDao,
    private val productGroupDao: ProductGroupDao,

) : BaseRepository(networkHandler), ProductRepository {

    override suspend fun getProducts(): Flow<NetworkResult<List<ProductEntity>>> {
        return getAsFlow<List<ProductEntity>>(ApiEndpoints.Products)
            .map { result ->
                result.map { response ->
                    productDao.insertAll(response)
                    response
                }

            }
    }

    override suspend fun getProductGroups(): Flow<NetworkResult<List<ProductGroupEntity>>> {
        return getAsFlow(ApiEndpoints.ProductGroups)
            .map { result ->
                result.map { response ->
                    productGroupDao.insertAll(response)
                    response
                }
            }
    }

    override suspend fun getProductUnits(): Flow<NetworkResult<List<ProductUnitEntity>>> {
        TODO("Not yet implemented")
    }
}