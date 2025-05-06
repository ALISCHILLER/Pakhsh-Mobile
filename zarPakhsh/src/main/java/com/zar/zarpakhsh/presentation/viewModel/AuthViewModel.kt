package com.zar.zarpakhsh.presentation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.error.NetworkResult
import com.zar.core.data.network.handler.NetworkResult
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

    private val _loginResult = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginResult: LiveData<NetworkResult<LoginResponse>> = _loginResult
    private val _validationState = MutableStateFlow<ValidationResult>(ValidationResult.Success)
    val validationState: StateFlow<ValidationResult> = _validationState

    fun validateAndLogin(username: String, password: String) {
        val validationResult = validateCredentialsUseCase(username, password)
        _validationState.value = validationResult

        if (validationResult is ValidationResult.Success) {
            // اگر اعتبارسنجی موفقیت‌آمیز بود، عملیات لاگین انجام شود
            login(username, password)
        }
    }

    /**
     * تابع برای اجرای عملیات لاگین.
     */
    fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        viewModelScope.launch {
            val result = loginUseCase.execute(loginRequest)
            _loginResult.value = result
        }
    }
}


