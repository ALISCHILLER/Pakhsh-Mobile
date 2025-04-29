package com.msa.zarpakhsh.di

import com.msa.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.msa.zarpakhsh.data.remote.RemoteDataSourceAuth
import com.msa.zarpakhsh.data.repository.AuthRepositoryImpl
import com.msa.zarpakhsh.domain.repository.AuthRepository
import com.msa.zarpakhsh.domain.usecase.LoginUseCase
import org.koin.dsl.module

val  CoreModule = module {
    single { LocalDataSourceAuth(get()) }

    // Singleton برای LocalDataSourceAuth
    single { LocalDataSourceAuth(get()) }

    // Singleton برای RemoteDataSourceAuth
    single { RemoteDataSourceAuth(get()) }



    // Factory برای LoginUseCase
    factory { LoginUseCase(get()) }
}