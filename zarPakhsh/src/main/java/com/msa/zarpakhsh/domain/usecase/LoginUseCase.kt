package com.msa.zarpakhsh.domain.usecase

import android.content.Context
import com.msa.core.data.network.handler.NetworkException
import com.msa.core.data.network.handler.NetworkResult
import com.msa.zarpakhsh.domain.entities.User
import com.msa.zarpakhsh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

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