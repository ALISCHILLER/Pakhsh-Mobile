package com.msa.zarpakhsh.di

import com.msa.zarpakhsh.data.repository.AuthRepositoryImpl
import com.msa.zarpakhsh.domain.repository.AuthRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Singleton برای AuthRepositoryImpl
    single<AuthRepository> { AuthRepositoryImpl(get(),get(),get()) }
}