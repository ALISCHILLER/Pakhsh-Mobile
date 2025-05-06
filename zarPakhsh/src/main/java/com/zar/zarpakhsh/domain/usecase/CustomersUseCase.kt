package com.zar.zarpakhsh.domain.usecase

import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow

class CustomersUseCase(
    private val customerRepository: CustomerRepository
) {

    suspend operator fun invoke(): Flow<NetworkResult<List<Customer>>> {
        return customerRepository.CustomersList()
    }
}