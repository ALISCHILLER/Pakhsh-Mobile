package com.zar.zarpakhsh.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.domain.entities.Customer
import com.zar.zarpakhsh.domain.usecase.CustomersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val customersUseCase: CustomersUseCase
) : ViewModel() {

    private val _customerState = MutableStateFlow<NetworkResult<List<Customer>>>(NetworkResult.Idle)
    val customerState: StateFlow<NetworkResult<List<Customer>>> = _customerState

    fun fetchCustomers() {
        viewModelScope.launch {
            customersUseCase.invoke().collect { state ->
                _customerState.value = state
            }
        }
    }
}
