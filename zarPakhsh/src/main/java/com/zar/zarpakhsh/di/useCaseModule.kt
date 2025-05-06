package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.domain.usecase.GetPokemonUseCase
import com.zar.zarpakhsh.domain.usecase.LoginUseCase
import com.zar.zarpakhsh.domain.usecase.ValidateCredentialsUseCase
import org.koin.dsl.module

val  useCaseModule = module {

    // Auth UseCases
    factory { LoginUseCase(get()) }
    factory { ValidateCredentialsUseCase(get()) }

    // Pokemon UseCases
    factory { GetPokemonUseCase(get()) }
}