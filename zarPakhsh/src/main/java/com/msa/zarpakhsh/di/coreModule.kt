package com.msa.zarpakhsh.di

import android.content.Context
import com.msa.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.msa.zarpakhsh.data.remote.RemoteDataSourceAuth
import com.msa.zarpakhsh.domain.usecase.LoginUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    // Singleton برای LocalDataSourceAuth
    single { LocalDataSourceAuth(get()) }
    // Singleton برای RemoteDataSourceAuth
    single { RemoteDataSourceAuth(get()) }
    single<Context> { androidApplication() }
}