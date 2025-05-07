package com.zar.zarpakhsh.domain.usecase

import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.local.entity.ProductEntity
import com.zar.zarpakhsh.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ProductUseCase(
    private val repository: ProductRepository
) {

    suspend operator fun invoke(): Flow<NetworkResult<List<ProductEntity>>> {
        return repository.getProducts()

    }
}