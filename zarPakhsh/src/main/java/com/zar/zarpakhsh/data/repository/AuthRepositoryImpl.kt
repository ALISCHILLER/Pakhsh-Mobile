package com.zar.zarpakhsh.data.repository

import android.content.Context
import com.zar.core.data.network.handler.NetworkException
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.data.mappers.toUser
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.remote.RemoteDataSourceAuth
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val remoteDataSourceAuth: RemoteDataSourceAuth,
    private val localDataSourceAuth: LocalDataSourceAuth,
    private val context: Context // تزریق Context از Koin
) : AuthRepository {



    override suspend fun login(username: String, password: String): NetworkResult<User> {
        return try {
            val result = remoteDataSourceAuth.login(LoginRequest(username, password))
            when (result) {
                is NetworkResult.Success -> {
                    localDataSourceAuth.saveUser(result.data.toUser())
                    NetworkResult.Success(
                        User(
                            id = result.data.userId,
                            username = result.data.username,
                            email = result.data.email,
                            token = result.data.token
                        )
                    )

                }
                is NetworkResult.Error -> {
                    throw NetworkException.fromStatusCode(result.httpCode, context)
                }
                else -> NetworkResult.Error.fromException(
                    Exception("Unexpected error occurred"),
                    context
                )
            }
        } catch (e: NetworkException) {
            NetworkResult.Error.fromException(e, context)
        }
    }

    override suspend fun logout(): NetworkResult<Unit> {
        return try {
            remoteDataSourceAuth.logout()
            NetworkResult.Success(Unit)
        } catch (e: NetworkException) {
            NetworkResult.Error.fromException(e, context)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        // بررسی وضعیت ورود کاربر (مثلاً از SharedPreferences یا TokenManager)
        val isLoggedIn = true // TODO: Check from SharedPreferences or TokenManager
        emit(isLoggedIn)
    }
}