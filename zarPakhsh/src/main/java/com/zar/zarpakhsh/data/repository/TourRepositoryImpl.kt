package com.zar.zarpakhsh.data.repository

import android.content.Context
import com.zar.core.data.network.handler.NetworkException
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.data.mappers.toUser
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.entities.Product
import com.zar.zarpakhsh.domain.entities.Settings
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.model.TourStep
import com.zar.zarpakhsh.domain.repository.TourRepository

class TourRepositoryImpl(
    private val  remoteDataSourceCustomer: RemoteDataSourceCustomer,
    private val context: Context
):TourRepository{


    override suspend fun getCustomerList(): NetworkResult<List<Customer>> {
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

    override suspend fun getProductList(): NetworkResult<List<Product>> {
        return NetworkResult.Error.fromException(Exception("API محصولات هنوز پیاده‌سازی نشده"), context, TourStep.FETCH_PRODUCTS.stepName)
    }

    override suspend fun getSettings(): NetworkResult<Settings> {
        return NetworkResult.Error.fromException(Exception("API تنظیمات هنوز پیاده‌سازی نشده"), context, TourStep.FETCH_SETTINGS.stepName)
    }
}