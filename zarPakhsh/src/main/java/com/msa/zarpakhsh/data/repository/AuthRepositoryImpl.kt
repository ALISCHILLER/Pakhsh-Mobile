package com.msa.zarpakhsh.data.repository

import com.msa.core.base.BaseRepository
import com.msa.core.data.network.handler.NetworkException
import com.msa.core.data.network.handler.NetworkHandler
import com.msa.core.data.network.handler.NetworkResult
import com.msa.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.msa.zarpakhsh.data.models.LoginRequest
import com.msa.zarpakhsh.data.remote.RemoteDataSourceAuth
import com.msa.zarpakhsh.domain.entities.User
import com.msa.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    networkHandler: NetworkHandler,
    private val localDataSource: LocalDataSourceAuth,
    private val remoteDataSource: RemoteDataSourceAuth
) : BaseRepository(networkHandler), AuthRepository {

    override suspend fun login(username: String, password: String): User? {
        return try {
            val response = remoteDataSource.login(LoginRequest(username, password))
            when (response) {
                is NetworkResult.Success -> {
                    val loginResponse = response.data
                    if (loginResponse != null) {
                        val user = User(
                            id = loginResponse.userId,
                            username = loginResponse.username,
                            email = loginResponse.email,
                            token = loginResponse.token
                        )
                        localDataSource.saveAuthToken(loginResponse.token)
                        localDataSource.saveUser(user)
                        user
                    } else {
                        throw NetworkException(-1, "اطلاعات ورود نادرست است.")
                    }
                }
                is NetworkResult.Error -> {
                    throw NetworkException(response.exception?.message ?: "خطای ناشناخته")
                }
                is NetworkResult.Loading -> {
                    throw NetworkException(-1, "عملیات در حال اجراست.")
                }
            }
        } catch (e: NetworkException) {
            throw e
        }
    }

    override suspend fun logout() {
        try {
            remoteDataSource.logout()
        } catch (e: Exception) {
            // در صورت نیاز می‌تونی لاگ بگیری ولی نذار جلوی حذف سشن محلی رو بگیره
        } finally {

        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return localDataSource.isLoggedIn()
    }
}
