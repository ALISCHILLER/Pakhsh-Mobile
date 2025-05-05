package com.zar.zarpakhsh.data.repository

import android.content.Context
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.model.TourStep
import com.zar.zarpakhsh.domain.repository.CustomerRepository

class CustomerRepositoryImpl(
    private val remoteDataSourceCustomer: RemoteDataSourceCustomer,
    private val context: Context
): CustomerRepository {
    override suspend fun CustomersList(): NetworkResult<List<Customer>> {
        return try {
            val result = remoteDataSourceCustomer.getCustomersList()
            when (result) {
                is NetworkResult.Success -> {
                    val customers = result.data.flatMap { it.customer }
                    NetworkResult.Success(customers)
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(
                        exception = result.exception,
                        message = result.message,
                        httpCode = result.httpCode,
                        retryCount = result.retryCount,
                        step = TourStep.FETCH_CUSTOMERS.stepName
                    )
                }
                else -> {
                    NetworkResult.Error.fromException(Exception("خطا در دریافت مشتریان"), context, TourStep.FETCH_CUSTOMERS.stepName)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error.fromException(e, context, TourStep.FETCH_CUSTOMERS.stepName)
        }
    }

    override suspend fun CustomerShiptoparty(): NetworkResult<List<Customer>> {
        return try {
            val result = remoteDataSourceCustomer.getCustomersList()
            when (result) {
                is NetworkResult.Success -> {
                    val customers = result.data.flatMap { it.customer }
                    NetworkResult.Success(customers)
                }
                is NetworkResult.Error -> {
                    NetworkResult.Error(
                        exception = result.exception,
                        message = result.message,
                        httpCode = result.httpCode,
                        retryCount = result.retryCount,
                        step = TourStep.FETCH_CUSTOMERS.stepName
                    )
                }
                else -> {
                    NetworkResult.Error.fromException(Exception("خطا در دریافت مشتریان"), context, TourStep.FETCH_CUSTOMERS.stepName)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error.fromException(e, context, TourStep.FETCH_CUSTOMERS.stepName)
        }
    }

}