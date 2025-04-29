package com.msa.zarpakhsh.domain.usecase


import com.msa.core.data.network.handler.ErrorHandler.handleNetworkError
import com.msa.core.data.network.handler.NetworkResult
import com.msa.zarpakhsh.domain.entities.User
import com.msa.zarpakhsh.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(username: String, password: String): NetworkResult<User> {
        return try {
            val user = authRepository.login(username, password)
            if (user != null) {
                NetworkResult.Success(user)
            } else {
                NetworkResult.Error(message = "اطلاعات ورود نادرست است.")
            }
        } catch (e: Exception) {
            handleNetworkError(e)
        }
    }
}