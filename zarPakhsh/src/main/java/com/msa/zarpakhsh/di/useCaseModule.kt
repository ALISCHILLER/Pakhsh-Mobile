package com.msa.zarpakhsh.di

import com.msa.zarpakhsh.domain.usecase.LoginUseCase
import com.msa.zarpakhsh.domain.usecase.ValidateCredentialsUseCase
import org.koin.dsl.module

val  useCaseModule = module {

    factory { LoginUseCase(get(), get()) }
    factory { ValidateCredentialsUseCase(get()) }

}