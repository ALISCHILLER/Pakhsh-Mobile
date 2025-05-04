package com.zar.zarpakhsh.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.handler.NetworkResult
import com.zar.zarpakhsh.domain.model.TourStep
import com.zar.zarpakhsh.domain.usecase.GetTourDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TourViewModel (
    private val getAllTourDataUseCase: GetTourDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<TourUiState>(TourUiState.Initial)
    val uiState: StateFlow<TourUiState> = _uiState
    private var totalSteps = TourStep.values().sumOf { it.apiCount }
    private var completedSteps = 0
    private val errorSteps = mutableListOf<TourStep>()


    fun loadTourData() {
        viewModelScope.launch {
            getAllTourDataUseCase(Unit)
                .onEach { result ->
                    updateProgress(result)
                }
                .launchAndCollectIn(this)
        }
    }

    private fun <T> updateProgress(result: NetworkResult<T>) {
        when (result) {
            is NetworkResult.Success -> {
                completedSteps++
                _uiState.value = TourUiState.Progress(
                    progress = completedSteps.toFloat() / totalSteps,
                    completedSteps = completedSteps,
                    totalSteps = totalSteps,
                    errors = errorSteps
                )
            }
            is NetworkResult.Error -> {
                errorSteps.add(result.step)
                _uiState.value = TourUiState.Progress(
                    progress = completedSteps.toFloat() / totalSteps,
                    completedSteps = completedSteps,
                    totalSteps = totalSteps,
                    errors = errorSteps
                )
            }
            else -> {}
        }
    }
}


sealed class TourUiState {
    object Initial : TourUiState()
    data class Progress(
        val progress: Float,
        val completedSteps: Int,
        val totalSteps: Int,
        val errors: List<TourStep>
    ) : TourUiState()

    data class Error(val message: String) : TourUiState()
    object Success : TourUiState()
}