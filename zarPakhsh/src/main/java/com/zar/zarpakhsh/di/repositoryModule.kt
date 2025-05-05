package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.data.repository.AuthRepositoryImpl
import com.zar.zarpakhsh.domain.repository.AuthRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Singleton برای AuthRepositoryImpl
    // AuthRepository
    factory<AuthRepository> {
        AuthRepositoryImpl(get())
    }
}