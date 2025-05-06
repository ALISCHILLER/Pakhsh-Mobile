package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.presentation.viewModel.AuthViewModel
import com.zar.zarpakhsh.presentation.viewModel.PokemonViewModel

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule= module {
    viewModel { PokemonViewModel(get()) }

}