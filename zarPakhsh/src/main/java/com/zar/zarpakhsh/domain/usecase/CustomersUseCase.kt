package com.zar.zarpakhsh.domain.usecase

import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import com.zar.zarpakhsh.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow

class CustomersUseCase(
    private val customerRepository: CustomerRepository
) {

    suspend operator fun invoke(): Flow<NetworkResult<List<CustomerModelEntity>>> {
        return customerRepository.CustomersList()
    }
}