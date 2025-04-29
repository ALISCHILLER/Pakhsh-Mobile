package com.msa.core.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * یک کلاس پایه برای تمامی ViewModel‌ها.
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * StateFlow برای مدیریت وضعیت عمومی (مانند Loading, Error, Success).
     */
    protected val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState: StateFlow<UIState> = _uiState

    /**
     * تنظیم وضعیت UI به Loading.
     */
    protected fun setLoading() {
        _uiState.value = UIState.Loading
    }

    /**
     * تنظیم وضعیت UI به Success.
     */
    protected fun <T> setSuccess(data: T) {
        _uiState.value = UIState.Success(data)
    }

    /**
     * تنظیم وضعیت UI به Error.
     */
    protected fun setError(errorMessage: String? = null) {
        _uiState.value = UIState.Error(errorMessage ?: "An unknown error occurred")
    }
}

/**
 * یک sealed class برای مدیریت وضعیت‌های مختلف UI.
 */
sealed class UIState {
    object Idle : UIState()
    object Loading : UIState()
    data class Success<T>(val data: T) : UIState()
    data class Error(val message: String) : UIState()
}