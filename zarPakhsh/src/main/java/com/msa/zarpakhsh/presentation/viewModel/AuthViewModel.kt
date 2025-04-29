package com.msa.zarpakhsh.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.core.data.network.handler.NetworkResult
import com.msa.zarpakhsh.domain.entities.User
import com.msa.zarpakhsh.domain.usecase.LoginUseCase
import com.msa.zarpakhsh.presentation.viewModel.LoginState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = loginUseCase(username, password)
            _loginState.value = when (result) {
                is NetworkResult.Success -> Success(result.data)
                is NetworkResult.Error -> Error(result.message ?: "خطای ناشناخته")
                NetworkResult.Loading -> TODO()
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}