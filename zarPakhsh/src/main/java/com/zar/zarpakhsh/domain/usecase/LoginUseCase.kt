package com.zar.zarpakhsh.domain.usecase

import android.content.Context
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val context: Context // تزریق Context از Koin
) {


    operator fun invoke(username: String, password: String): Flow<NetworkResult<User>> = flow {
        emit(NetworkResult.Loading)
        try {
            val result = authRepository.login(username, password)
            emit(result)
        } catch (e: Exception) {
            emit(NetworkResult.Error.fromException(e, context))
        }
    }
}