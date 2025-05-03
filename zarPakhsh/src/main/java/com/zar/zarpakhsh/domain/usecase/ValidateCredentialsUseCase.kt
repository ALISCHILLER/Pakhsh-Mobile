package com.zar.zarpakhsh.domain.usecase

import android.content.Context
import com.zar.zarpakhsh.R

class ValidateCredentialsUseCase(
    private val context: Context // تزریق Context از Koin
) {
    operator fun invoke(username: String, password: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Error(context.getString(R.string.validation_error_username_empty))
            password.isBlank() -> ValidationResult.Error(context.getString(R.string.validation_error_password_empty))
            password.length < 6 -> ValidationResult.Error(context.getString(R.string.validation_error_password_length))
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}