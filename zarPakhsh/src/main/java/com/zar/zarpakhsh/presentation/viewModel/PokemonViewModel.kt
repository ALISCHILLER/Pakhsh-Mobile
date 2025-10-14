package com.zar.zarpakhsh.presentation.viewModel




import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.domain.entities.Pokemon
import com.zar.zarpakhsh.domain.usecase.GetPokemonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PokemonViewModel(
    private val getPokemonUseCase: GetPokemonUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NetworkResult<Pokemon>>(NetworkResult.Idle)
    val state: StateFlow<NetworkResult<Pokemon>> = _state
    init {
        fetchDitto()
    }

    fun fetchDitto() {
        viewModelScope.launch {
            getPokemonUseCase().collect {
                _state.value = it
            }
        }
    }
}