package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.domain.usecase.GetPokemonUseCase
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val useCaseModule = module {
//    factory { GetPokemonUseCase(get()) }

    single { GetPokemonUseCase(get()) }.withOptions {
        createdAtStart()
    }
}