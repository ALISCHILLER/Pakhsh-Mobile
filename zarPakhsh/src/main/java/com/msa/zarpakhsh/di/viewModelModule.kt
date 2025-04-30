package com.msa.zarpakhsh.di

import com.msa.zarpakhsh.presentation.viewModel.AuthViewModel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule= module {
    viewModel { AuthViewModel(get()) }
}