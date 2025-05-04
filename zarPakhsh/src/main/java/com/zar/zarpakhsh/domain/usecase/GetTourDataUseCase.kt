package com.zar.zarpakhsh.domain.usecase

import android.content.Context
import com.zar.core.data.network.handler.NetworkResult
import com.zar.core.data.network.handler.NetworkResult.*
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.model.TourStep
import com.zar.zarpakhsh.domain.repository.TourRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetTourDataUseCase (
    private val tourRepository: TourRepository,
    private val context: Context // تزریق Context از Koin
) {
    suspend operator fun invoke() : Flow<NetworkResult<List<Any>>> = flow {
        val results = mutableListOf<Any>()
        // 1. Fetch Customers
        // 1. Fetch Customers
        when (val result = tourRepository.getCustomerList()) {
            is NetworkResult.Success -> {
                results.add(result.data)
                emit(Success(result.data, TourStep.FETCH_CUSTOMERS.stepName))
            }
            is NetworkResult.Error -> {
                emit(Error(result.exception, result.message, result.httpCode, result.retryCount, TourStep.FETCH_CUSTOMERS.stepName, result.canRetry))
            }

            NetworkResult.Idle -> emit(Idle)
            NetworkResult.Loading -> emit(Loading)
        }



    }

}