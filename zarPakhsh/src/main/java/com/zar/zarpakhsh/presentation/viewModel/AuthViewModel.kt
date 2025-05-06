package com.zar.zarpakhsh.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.models.LoginRequest
import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.domain.entities.User
import com.zar.zarpakhsh.domain.usecase.LoginUseCase
import com.zar.zarpakhsh.domain.usecase.ValidateCredentialsUseCase
import com.zar.zarpakhsh.domain.usecase.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * یک sealed class برای مدیریت حالت‌های مختلف وضعیت لاگین در ViewModel.
 */
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val validateCredentialsUseCase: ValidateCredentialsUseCase
) : ViewModel() {

    // State for validation and login result
    private val _validationState = MutableStateFlow<ValidationResult>(ValidationResult.Success)
    val validationState: StateFlow<ValidationResult> = _validationState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun validateAndLogin(username: String, password: String) {
        val validationResult = validateCredentialsUseCase(username, password)
        _validationState.value = validationResult

        if (validationResult is ValidationResult.Success) {
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            when (val result = loginUseCase.execute(loginRequest)) {
                is NetworkResult.Success<User> -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _loginState.value = LoginState.Error(result.error.message)
                }
                is NetworkResult.Loading -> {
                    _loginState.value = LoginState.Loading
                }
                is NetworkResult.Idle -> {
                    _loginState.value = LoginState.Idle
                }
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
