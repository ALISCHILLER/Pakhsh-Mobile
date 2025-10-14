package com.zar.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.error.AppError
import com.zar.core.data.network.result.NetworkMetadata
import com.zar.core.data.network.result.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * یک کلاس پایه برای تمامی ViewModel‌ها.
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * StateFlow برای مدیریت وضعیت عمومی (مانند Loading, Error, Success).
     */
    private val _uiState = MutableStateFlow<UIState<*>>(UIState.Idle)
    val uiState: StateFlow<UIState<*>> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

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
        _uiState.value = UIState.Error(
            UnknownError(message = errorMessage ?: "An unknown error occurred"),
        )
    }

    /**
     * تنظیم وضعیت UI به Error با مدل خطای دامین.
     */
    protected fun setError(appError: AppError, cause: Throwable? = null) {
        _uiState.value = UIState.Error(appError, cause)
    }

    /**
     * تنظیم وضعیت UI به Idle.
     */
    protected fun setIdle() {
        _uiState.value = UIState.Idle
    }

    protected fun updateUiState(transform: (UIState<*>) -> UIState<*>) {
        _uiState.update(transform)
    }

    protected inline fun <reified T> updateSuccess(noinline transform: (T) -> T) {
        _uiState.update { current ->
            if (current is UIState.Success<*>) {
                val data = current.data
                if (data is T) {
                    UIState.Success(transform(data))
                } else {
                    current
                }
            } else {
                current
            }
        }
    }

    /**
     * اجرای یک بلاک در [viewModelScope] و مدیریت نتیجه‌ی [NetworkResult] برای UI.
     */
    protected fun <T> collectNetworkResult(
        source: Flow<NetworkResult<T>>,
        onSuccess: (T, NetworkMetadata) -> Unit = { data, _ -> setSuccess(data) },
        onError: (AppError, Throwable?, NetworkMetadata) -> Unit = { error, cause, _ ->
            setError(error, cause)
        },
        onLoading: () -> Unit = { setLoading() },
        onIdle: () -> Unit = { setIdle() },
        onCompletion: (() -> Unit)? = null,
        scope: CoroutineScope = viewModelScope,
        collectLatest: Boolean = true,
    ): Job {
        val collector: suspend (NetworkResult<T>) -> Unit = { result ->
            handleNetworkResult(result, onSuccess, onError, onLoading, onIdle)
        }
        return scope.launch {
            try {
                if (collectLatest) {
                    source.collectLatest(collector)
                } else {
                    source.collect(collector)
                }
            } finally {
                onCompletion?.invoke()
            }
        }
    }

    protected fun <T> launchNetworkRequest(
        showLoading: Boolean = true,
        emitIdleBeforeStart: Boolean = false,
        scope: CoroutineScope = viewModelScope,
        block: suspend () -> NetworkResult<T>,
        onSuccess: (T, NetworkMetadata) -> Unit = { data, _ -> setSuccess(data) },
        onError: (AppError, Throwable?, NetworkMetadata) -> Unit = { error, cause, _ ->
            setError(error, cause)
        },
        onIdle: () -> Unit = { setIdle() },
        onCompletion: (() -> Unit)? = null,
    ): Job {
        return scope.launch {
            if (emitIdleBeforeStart) {
                onIdle()
            }
            if (showLoading) {
                onLoading()
            }
            try {
                val result = block()
                handleNetworkResult(result, onSuccess, onError, onLoading, onIdle)
            } finally {
                onCompletion?.invoke()
            }
        }
    }

    protected fun <T> handleNetworkResult(
        result: NetworkResult<T>,
        onSuccess: (T, NetworkMetadata) -> Unit,
        onError: (AppError, Throwable?, NetworkMetadata) -> Unit,
        onLoading: () -> Unit,
        onIdle: () -> Unit,
    ) {
        when (result) {
            is NetworkResult.Success -> onSuccess(result.data, result.metadata)
            is NetworkResult.Error -> onError(result.error, result.cause, result.metadata)
            is NetworkResult.Loading -> onLoading()
            is NetworkResult.Idle -> onIdle()
        }
    }

    protected suspend fun emitEvent(event: UiEvent) {
        _events.emit(event)
    }

    protected fun tryEmitEvent(event: UiEvent): Boolean {
        return _events.tryEmit(event)
    }
}

/**
 * یک sealed class برای مدیریت وضعیت‌های مختلف UI.
 */
sealed class UIState<out T> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val error: AppError, val cause: Throwable? = null) : UIState<Nothing>()
}

interface UiEvent

val UIState<*>.isIdle: Boolean
    get() = this is UIState.Idle

val UIState<*>.isLoading: Boolean
    get() = this is UIState.Loading

val UIState<*>.isSuccess: Boolean
    get() = this is UIState.Success<*>

val UIState<*>.isError: Boolean
    get() = this is UIState.Error

@Suppress("UNCHECKED_CAST")
fun <T> UIState<T>.dataOrNull(): T? = (this as? UIState.Success<T>)?.data

fun UIState<*>.errorOrNull(): AppError? = (this as? UIState.Error)?.error