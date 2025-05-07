package com.zar.zarpakhsh.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.di.useCaseModule
import com.zar.zarpakhsh.di.viewModelModule
import com.zar.zarpakhsh.presentation.viewModel.PokemonViewModel
import com.zar.zarpakhsh.domain.entities.Pokemon // فرض بر اینه اینجاست
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin

@Composable
fun PokemonScreen(viewModel: PokemonViewModel = koinViewModel<PokemonViewModel>()) {

    val state by viewModel.state.collectAsState()

    when (val result = state) {
        is NetworkResult.Success<Pokemon> -> {
            val pokemon = result.data
            Text("Pokemon Name: ${pokemon.name}")
        }
        is NetworkResult.Error -> {
            val error = (state as NetworkResult.Error).error
            Column(Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(25.dp)
            ){
                Text(text = "Error: ${error.message}", color = Color.Red)
            }

        }
        is NetworkResult.Loading -> {
            CircularProgressIndicator()
        }
        else -> {}
    }
}