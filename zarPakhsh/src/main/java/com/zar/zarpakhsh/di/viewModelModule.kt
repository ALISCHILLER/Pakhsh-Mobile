package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.presentation.viewModel.PokemonViewModel
import org.koin.core.module.dsl.createdAtStart

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val viewModelModule= module {
    factory { PokemonViewModel(get()) } withOptions {
        createdAtStart()
    }

//    viewModel { PokemonViewModel(get()) }
}